package sky.kr.co.newtogetusa.ui.main.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentSelectDateRangeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// iOS SelectDateRangeViewController 대응: 픽업 가능일 범위 선택 풀스크린 캘린더
@AndroidEntryPoint
class SelectDateRangeFragment :
    BaseFragment<FragmentSelectDateRangeBinding, SelectDateRangeViewModel>() {

    override val layoutId: Int = R.layout.fragment_select_date_range
    override val viewModel: SelectDateRangeViewModel by viewModels()

    private val dayCells = mutableListOf<DayCell>()
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var areaIndex: Int = 0

    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val resultFormatter: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE

    override fun init() {
        super.init()

        areaIndex = arguments?.getInt("areaIndex", 0) ?: 0
        startDate = parseDate(arguments?.getString("startDate"))
        endDate = parseDate(arguments?.getString("endDate"))

        dataBinding.ivClose.setOnClickListener { findNavController().popBackStack() }
        dataBinding.tvDone.setOnClickListener { onDone() }

        buildMonths()
        updateSelection()
        updateDoneButton()
    }

    private fun buildMonths() {
        val inflater = LayoutInflater.from(requireContext())
        val today = LocalDate.now()
        val firstMonth = YearMonth.from(today)

        // 현재 월부터 12개월 (iOS monthCount = 12)
        repeat(12) { offset ->
            val month = firstMonth.plusMonths(offset.toLong())
            val monthView =
                inflater.inflate(R.layout.item_select_date_range_month, dataBinding.llMonths, false)
            monthView.findViewById<TextView>(R.id.tvMonthTitle).text =
                "${month.year}년 ${month.monthValue}월"
            val daysContainer = monthView.findViewById<LinearLayout>(R.id.llDays)
            buildMonthDays(inflater, daysContainer, month)
            dataBinding.llMonths.addView(monthView)
        }
    }

    private fun buildMonthDays(inflater: LayoutInflater, container: LinearLayout, month: YearMonth) {
        // 일요일 시작 (iOS weekday: 일=1)
        val firstWeekday = month.atDay(1).dayOfWeek.value % 7
        val totalDays = month.lengthOfMonth()
        val rows = (firstWeekday + totalDays + 6) / 7

        var rowLayout: LinearLayout? = null
        for (slot in 0 until rows * 7) {
            if (slot % 7 == 0) {
                rowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                container.addView(
                    rowLayout,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            val dayNum = slot - firstWeekday + 1
            val date = if (dayNum in 1..totalDays) month.atDay(dayNum) else null

            val cellView =
                inflater.inflate(R.layout.item_select_date_range_day, rowLayout, false)
            rowLayout?.addView(
                cellView,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )

            val tvDay = cellView.findViewById<TextView>(R.id.tvDay)
            if (date == null) {
                tvDay.text = ""
            } else {
                tvDay.text = date.dayOfMonth.toString()
                cellView.setOnClickListener { handleDateSelection(date) }
            }

            dayCells.add(
                DayCell(
                    date = date,
                    root = cellView,
                    tvDay = tvDay,
                    rangeBg = cellView.findViewById(R.id.vRangeBg),
                    maskLeft = cellView.findViewById(R.id.vMaskLeft),
                    maskRight = cellView.findViewById(R.id.vMaskRight)
                )
            )
        }
    }

    private fun handleDateSelection(date: LocalDate) {
        val today = LocalDate.now()
        if (date.isBefore(today)) return

        val start = startDate
        val end = endDate
        when {
            start == null -> {
                startDate = date
                endDate = null
            }

            end == null -> {
                if (date.isBefore(start)) {
                    startDate = date
                } else {
                    endDate = date
                }
            }

            else -> {
                startDate = date
                endDate = null
            }
        }

        updateSelection()
        updateDoneButton()
    }

    private fun updateSelection() {
        val today = LocalDate.now()
        val start = startDate
        val end = endDate
        val singleDay = start != null && end != null && start == end

        dayCells.forEach { cell ->
            val date = cell.date ?: return@forEach

            cell.rangeBg.isVisible = false
            cell.maskLeft.isVisible = false
            cell.maskRight.isVisible = false
            cell.tvDay.background = null
            cell.root.isEnabled = !date.isBefore(today)

            cell.tvDay.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when {
                        date.isBefore(today) -> R.color.black_20
                        date == today -> R.color.primary_100
                        date.dayOfWeek == DayOfWeek.SUNDAY ||
                            date.dayOfWeek == DayOfWeek.SATURDAY -> R.color.primary_100

                        else -> R.color.black_80
                    }
                )
            )
            if (date == today) {
                cell.tvDay.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.background_s_p10_r20
                )
            }

            if (start != null) {
                when {
                    date == start -> {
                        setSelectedCircle(cell)
                        // 시작일: 범위 배경 + 왼쪽 절반 흰색 마스크 (iOS와 동일)
                        if (end != null && !singleDay) {
                            cell.rangeBg.isVisible = true
                            cell.maskLeft.isVisible = true
                        }
                    }

                    end != null && date == end -> {
                        setSelectedCircle(cell)
                        // 마지막일: 범위 배경 + 오른쪽 절반 흰색 마스크 (iOS와 동일)
                        if (!singleDay) {
                            cell.rangeBg.isVisible = true
                            cell.maskRight.isVisible = true
                        }
                    }

                    end != null && date.isAfter(start) && date.isBefore(end) -> {
                        cell.rangeBg.isVisible = true
                    }
                }
            }
        }
    }

    private fun setSelectedCircle(cell: DayCell) {
        cell.tvDay.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.background_s_b80_r20
        )
        cell.tvDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun updateDoneButton() {
        val start = startDate
        val end = endDate
        val complete = start != null && end != null

        // 배경/텍스트 색상은 selector가 enabled 상태에 따라 자동 변경 (비활성: primary_40 + 흰색40%)
        dataBinding.tvDone.isEnabled = complete
        dataBinding.tvDone.text = if (complete) {
            "${start!!.format(displayFormatter)} - ${end!!.format(displayFormatter)}"
        } else {
            "마지막 날짜를 선택해 주세요."
        }
    }

    private fun onDone() {
        val start = startDate ?: return
        val end = endDate ?: return

        val bundle = Bundle().apply {
            putInt("areaIndex", areaIndex)
            putString("startDate", start.format(resultFormatter))
            putString("endDate", end.format(resultFormatter))
        }
        requireActivity().supportFragmentManager.setFragmentResult("fromSelectDateRange", bundle)
        findNavController().popBackStack()
    }

    private fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(raw, resultFormatter) }.getOrNull()
    }

    private data class DayCell(
        val date: LocalDate?,
        val root: View,
        val tvDay: TextView,
        val rangeBg: View,
        val maskLeft: View,
        val maskRight: View
    )
}
