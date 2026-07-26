package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.CalendarDayLayoutBinding
import sky.kr.co.newtogetusa.databinding.CalendarDayTitleContainerBinding
import sky.kr.co.newtogetusa.databinding.DialogBottomDateRangeBinding
import sky.kr.co.newtogetusa.utils.week
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class BottomDateRangeDialog :
    BottomBaseDialog<DialogBottomDateRangeBinding, BottomCalendarViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_date_range
    override val viewModel: BottomCalendarViewModel by viewModels()

    private lateinit var todayDate: LocalDate
    private lateinit var startMonth: YearMonth
    private lateinit var endMonth: YearMonth
    private lateinit var currentMonth: YearMonth

    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
    var rangeSelectCallback: ((start: LocalDate, end: LocalDate) -> Unit)? = null

    override fun init() {
        super.init()
        todayDate = LocalDate.now()
        currentMonth = YearMonth.now()
        startMonth = currentMonth.minusMonths(12)
        endMonth = currentMonth.plusMonths(12)
        dataBinding.tvYearMonth.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"

        dataBinding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)

                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    if (container.titlesContainer.tag == null) {
                        container.titlesContainer.tag = data.yearMonth
                        container.titlesContainer.children.map { it as TextView }
                            .forEachIndexed { index, textView ->
                                textView.text = week[index]
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        if (index == 0) R.color.red_100
                                        else if (index == 6) R.color.blue_100
                                        else R.color.black_60
                                    )
                                )
                            }
                    }
                }
            }

        dataBinding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val date = data.date
                val isToday = date == todayDate
                val isStart = date == startDate
                val isEnd = date == endDate
                val inRange = startDate != null && endDate != null &&
                    date.isAfter(startDate) && date.isBefore(endDate)
                val isPast = date.isBefore(todayDate)

                container.binding.tvDay.apply {
                    text = date.dayOfMonth.toString()
                    visibility =
                        if (data.position == DayPosition.MonthDate) View.VISIBLE else View.INVISIBLE
                    background = when {
                        isStart || isEnd ->
                            context.getDrawable(R.drawable.background_s_b80_r20)
                        inRange ->
                            context.getDrawable(R.drawable.background_s_p10_r20)
                        isToday ->
                            context.getDrawable(R.drawable.background_s_p10_r20)
                        else -> null
                    }
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            when {
                                isStart || isEnd -> R.color.white
                                isPast -> R.color.black_20
                                isToday -> R.color.primary_100
                                date.dayOfWeek == java.time.DayOfWeek.SUNDAY -> R.color.red_100
                                date.dayOfWeek == java.time.DayOfWeek.SATURDAY -> R.color.blue_100
                                else -> R.color.black_60
                            }
                        )
                    )
                }
            }
        }

        dataBinding.calendarView.setup(startMonth, endMonth, firstDayOfWeekFromLocale())
        dataBinding.calendarView.scrollToMonth(currentMonth)
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.ivPrevMonth.setOnClickListener {
            val prevMonth = currentMonth.minusMonths(1)
            if (!prevMonth.isBefore(startMonth)) {
                dataBinding.calendarView.smoothScrollToMonth(prevMonth)
            }
        }

        dataBinding.ivNextMonth.setOnClickListener {
            val nextMonth = currentMonth.plusMonths(1)
            if (!nextMonth.isAfter(endMonth)) {
                dataBinding.calendarView.smoothScrollToMonth(nextMonth)
            }
        }

        dataBinding.calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            dataBinding.tvYearMonth.text = "${currentMonth.year}년 ${
                currentMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)
            }"
        }

        dataBinding.tvSelectedComplete.setOnClickListener {
            val start = startDate
            val end = endDate
            if (start != null && end != null) {
                rangeSelectCallback?.invoke(start, end)
                dismissAllowingStateLoss()
            } else {
                dismissAllowingStateLoss()
            }
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val binding = CalendarDayLayoutBinding.bind(view)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position != DayPosition.MonthDate || day.date.isBefore(todayDate)) {
                    return@setOnClickListener
                }

                val start = startDate
                val end = endDate
                when {
                    // 첫 선택 또는 초기화 후 재선택
                    start == null || (start != null && end != null) -> {
                        startDate = day.date
                        endDate = null
                    }
                    // 시작일 이후 날짜 선택 → 종료일
                    day.date.isAfter(start) -> {
                        endDate = day.date
                    }
                    // 시작일 이전 날짜 선택 → 시작일 변경
                    else -> {
                        startDate = day.date
                        endDate = null
                    }
                }
                dataBinding.calendarView.notifyCalendarChanged()
            }
        }
    }

    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val binding = CalendarDayTitleContainerBinding.bind(view)
        val titlesContainer = view as ViewGroup
    }
}
