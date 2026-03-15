package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.CalendarDayLayoutBinding
import sky.kr.co.newtogetusa.databinding.CalendarDayTitleContainerBinding
import sky.kr.co.newtogetusa.databinding.FragmentHistoryDeliveryBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.week
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar

@AndroidEntryPoint
class HistoryDeliveryFragment : BaseFragment<FragmentHistoryDeliveryBinding, HistoryDeliveryViewModel>() {
    override val layoutId: Int = R.layout.fragment_history_delivery
    override val viewModel: HistoryDeliveryViewModel by viewModels()

    private lateinit var historyAdapter: HistoryDeliverAdapter
    private val todayDate = LocalDate.now()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.tvTopAll.isSelected = true
        historyAdapter = HistoryDeliverAdapter(viewModel)

        dataBinding.rvHistory.apply {
            adapter = historyAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
        initCalendar()
        //savedState = dataBinding.rvHistory.layoutManager?.onSaveInstanceState()
    }

    private fun initCalendar() {
        val calendar = Calendar.getInstance()
        dataBinding.icCalendar.tvYearMonth.text = "${calendar.get(Calendar.YEAR)}년 ${calendar.get(Calendar.MONTH) + 1}월"

        dataBinding.icCalendar.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: android.view.View) = MonthViewContainer(view)

                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    if (container.titlesContainer.tag == null) {
                        container.titlesContainer.tag = data.yearMonth
                        container.binding.root.forEachIndexed { index, titleView ->
                            (titleView as? android.widget.TextView)?.apply {
                                text = week[index]
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        when (index) {
                                            0 -> R.color.red_100
                                            6 -> R.color.blue_100
                                            else -> R.color.black_60
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }

        dataBinding.icCalendar.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: android.view.View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.binding.tvDay.apply {
                    text = data.date.dayOfMonth.toString()
                    visibility =
                        if (data.position == DayPosition.MonthDate) android.view.View.VISIBLE else android.view.View.INVISIBLE
                    background =
                        if (data.date == todayDate) context.getDrawable(R.drawable.background_s_p10_r20) else null
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            when (data.date.dayOfWeek) {
                                DayOfWeek.SUNDAY -> R.color.red_100
                                DayOfWeek.SATURDAY -> R.color.blue_100
                                else -> if (data.date == todayDate) R.color.white else R.color.black_60
                            }
                        )
                    )
                }
            }
        }

        val currentMonth = YearMonth.now()
        dataBinding.icCalendar.calendarView.setup(currentMonth, currentMonth, firstDayOfWeekFromLocale())
        dataBinding.icCalendar.calendarView.scrollToMonth(currentMonth)
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isModePlayer
                        .filterNotNull()
                        .distinctUntilChanged()
                        .collectLatest {
                        Timber.d("isModePlayer $it")
                        val navController = findNavController()
                        if (
                            !it &&
                            navController.currentDestination?.id == R.id.historyDeliveryFragment
                        ) {
                            navController.navigate(R.id.action_historyDeliveryFragment_to_historyFragment)
                        }
                    }
                }

                launch {
                    viewModel.deliveryPagingFlow.collectLatest { pagingData ->
                        historyAdapter.submitData(pagingData)
                    }
                }
            }
        }
    }

    inner class DayViewContainer(view: android.view.View) : ViewContainer(view) {
        val binding = CalendarDayLayoutBinding.bind(view)
    }

    inner class MonthViewContainer(view: android.view.View) : ViewContainer(view) {
        val binding = CalendarDayTitleContainerBinding.bind(view)
        val titlesContainer = binding.root
    }
}