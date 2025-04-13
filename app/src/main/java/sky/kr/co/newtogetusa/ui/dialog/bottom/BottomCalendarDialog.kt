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
import sky.kr.co.newtogetusa.databinding.DialogBottomCalendarBinding
import sky.kr.co.newtogetusa.utils.week
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BottomCalendarDialog :
    BottomBaseDialog<DialogBottomCalendarBinding, BottomCalendarViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_calendar
    override val viewModel: BottomCalendarViewModel by viewModels()

    private lateinit var todayDate: LocalDate
    var selectedDate: LocalDate? = null
    var daySelectCallback: ((LocalDate) -> Unit)? = null

    @SuppressLint("SetTextI18n")
    override fun init() {
        super.init()
        todayDate = LocalDate.now()

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        dataBinding.tvYearMonth.text = "${year}년 ${month}월"

        dataBinding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)

                override fun bind(
                    container: MonthViewContainer,
                    data: CalendarMonth
                ) {
                    if (container.titlesContainer.tag == null) {
                        container.titlesContainer.tag = data.yearMonth
                        container.titlesContainer.children.map { it as TextView }
                            .forEachIndexed { index, textView ->
                                textView.text = week[index]
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        if (index == 0) R.color.red_100 else if (index == 6) R.color.blue_100 else R.color.black_60
                                    )
                                )
                            }
                    }
                }

            }

        dataBinding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun bind(
                container: DayViewContainer,
                data: CalendarDay
            ) {
                val day = data
                container.day = data
                val isToday = day.date == todayDate
                val isSelectedDay = day.date == selectedDate
                container.binding.root.apply {
                    background = if (isSelectedDay) null
                    else null
                }
                container.binding.tvDay.apply {
                    text = data.date.dayOfMonth.toString()
                    background =
                        if (isToday) context.getDrawable(R.drawable.background_s_p100_r20) else null
                    visibility =
                        if (data.position == DayPosition.MonthDate) View.VISIBLE else View.INVISIBLE
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (isSelectedDay) R.color.white
                            else if(isToday)R.color.white
                            else if (day.date.dayOfWeek == firstDayOfWeekFromLocale()) R.color.red_100
                            else if (day.date.dayOfWeek == firstDayOfWeekFromLocale().plus(6)) R.color.blue_100
                            else R.color.black_60
                        )
                    )
                    Timber.d("dayofWeek : ${day.date.dayOfWeek}, day : $day")
                }
            }
        }


        val startMonthCalendar = Calendar.getInstance()
        val endMonth = YearMonth.now()
        val currentMonth = YearMonth.now()
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val startMonth = YearMonth.of(
            startMonthCalendar.get(
                Calendar.YEAR
            ), startMonthCalendar.get(
                Calendar.MONTH
            ) + 1
        )
        dataBinding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        dataBinding.calendarView.scrollToMonth(currentMonth)
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.calendarView.monthScrollListener = {
            val month = dataBinding.calendarView.findFirstVisibleMonth()?.yearMonth
            Timber.d("year ${month?.year}, month ${month?.month}")
            dataBinding.tvYearMonth.text = "${month?.year.toString()}년 ${
                month?.month?.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                )
            }"
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val binding = CalendarDayLayoutBinding.bind(view)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    // Keep a reference to any previous selection
                    // in case we overwrite it and need to reload it.
                    val currentSelection = selectedDate
                    if (currentSelection == day.date) {
                        // If the user clicks the same date, clear selection.
                        selectedDate = null
                        // Reload this date so the dayBinder is called
                        // and we can REMOVE the selection background.
                        dataBinding.calendarView.notifyDateChanged(currentSelection)
                    } else {
                        selectedDate = day.date
                        // Reload the newly selected date so the dayBinder is
                        // called and we can ADD the selection background.
                        dataBinding.calendarView.notifyDateChanged(day.date)
                        if (currentSelection != null) {
                            // We need to also reload the previously selected
                            // date so we can REMOVE the selection background.
                            dataBinding.calendarView.notifyDateChanged(currentSelection)
                        }
                    }
                    selectedDate?.let {
                        //daySelectCallback?.invoke(it)
                        //dismissAllowingStateLoss()
                    }
                }
            }
        }

    }

    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val binding = CalendarDayTitleContainerBinding.bind(view)

        // Alternatively, you can add an ID to the container layout and use findViewById()
        val titlesContainer = view as ViewGroup
    }
}