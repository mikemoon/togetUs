package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.forEachIndexed
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerDeliveryHistoryReq
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
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class HistoryDeliveryFragment : BaseFragment<FragmentHistoryDeliveryBinding, HistoryDeliveryViewModel>() {
    override val layoutId: Int = R.layout.fragment_history_delivery
    override val viewModel: HistoryDeliveryViewModel by viewModels()

    private lateinit var historyAdapter: HistoryDeliverAdapter
    private lateinit var calendarBottomSheetAdapter: HistoryDeliverAdapter
    private val todayDate = LocalDate.now()
    private var selectedDate: LocalDate? = null
    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<android.widget.LinearLayout>
    private var bottomSheetExpandedTop = 0
    private var bottomSheetCollapsedTop = 0

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.tvTopAll.isSelected = true
        historyAdapter = HistoryDeliverAdapter(viewModel) { selectedItem ->
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.historyDeliveryFragment) {
                val action = HistoryDeliveryFragmentDirections
                    .actionHistoryDeliveryFragmentToPlayerHistoryDetailFragment(
                        selectedItem.deliveryId.toLong()
                    )
                navController.navigate(action)
            }
        }

        dataBinding.rvHistory.apply {
            adapter = historyAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
        calendarBottomSheetAdapter = HistoryDeliverAdapter(viewModel) { selectedItem ->
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.historyDeliveryFragment) {
                val action = HistoryDeliveryFragmentDirections
                    .actionHistoryDeliveryFragmentToPlayerHistoryDetailFragment(
                        selectedItem.deliveryId.toLong()
                    )
                navController.navigate(action)
            }
        }
        dataBinding.rvCalendarBottomSheet.apply {
            adapter = calendarBottomSheetAdapter
            addItemDecoration(VerticalSpaceItemDecoration(12.dpToPx()))
        }
        initBottomSheet()
        initCalendar()
        //savedState = dataBinding.rvHistory.layoutManager?.onSaveInstanceState()
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(dataBinding.calendarBottomSheet).apply {
            isFitToContents = false
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: android.view.View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    snapBottomSheet(bottomSheet.top)
                }
            }

            override fun onSlide(bottomSheet: android.view.View, slideOffset: Float) = Unit
        })
        dataBinding.calendarBottomSheet.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP ||
                event.action == android.view.MotionEvent.ACTION_CANCEL
            ) {
                dataBinding.calendarBottomSheet.post {
                    snapBottomSheet(dataBinding.calendarBottomSheet.top)
                }
            }
            false
        }
        dataBinding.root.doOnLayout { updateBottomSheetLayout() }
        dataBinding.icCalendar.calendarContentContainer.doOnLayout { updateBottomSheetLayout() }
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
                container.day = data
                val isSelectedDay = data.date == selectedDate
                val isToday = data.date == todayDate
                val deliveryDots = getDeliveryDots(data.date, data.position == DayPosition.MonthDate)

                container.binding.tvDay.apply {
                    text = data.date.dayOfMonth.toString()
                    visibility =
                        if (data.position == DayPosition.MonthDate) android.view.View.VISIBLE else android.view.View.INVISIBLE
                    background = when {
                        isSelectedDay -> context.getDrawable(R.drawable.background_s_p100_r20)
                        isToday -> context.getDrawable(R.drawable.background_s_p10_r20)
                        else -> null
                    }
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            when {
                                isSelectedDay -> R.color.white
                                isToday -> R.color.primary_80
                                data.date.dayOfWeek == DayOfWeek.SUNDAY -> R.color.red_100
                                data.date.dayOfWeek == DayOfWeek.SATURDAY -> R.color.blue_100
                                else -> R.color.black_60
                            }
                        )
                    )
                }
                bindDeliveryDots(container.binding, deliveryDots)
            }
        }

        currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        dataBinding.icCalendar.calendarView.setup(startMonth, endMonth, firstDayOfWeekFromLocale())
        dataBinding.icCalendar.calendarView.scrollToMonth(currentMonth)
        dataBinding.icCalendar.ivPrevMonth.setOnClickListener {
            dataBinding.icCalendar.calendarView.smoothScrollToMonth(currentMonth.minusMonths(1))
        }
        dataBinding.icCalendar.ivNextMonth.setOnClickListener {
            dataBinding.icCalendar.calendarView.smoothScrollToMonth(currentMonth.plusMonths(1))
        }
        dataBinding.icCalendar.calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            dataBinding.icCalendar.tvYearMonth.text = "${month.yearMonth.year}년 ${month.yearMonth.month.getDisplayName(
                TextStyle.FULL, Locale.KOREAN)}"
            viewModel.getMonthInfo(month.yearMonth.year, month.yearMonth.monthValue)
            dataBinding.calendarBottomSheet.post { updateBottomSheetLayout() }
        }
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
                        if (!viewModel.isShowCalendar.value) {
                            historyAdapter.submitData(pagingData)
                        }
                    }
                }

                launch {
                    viewModel.isShowCalendar
                        .collectLatest { isShowCalendar ->
                        if (isShowCalendar) {
                            viewModel.getMonthInfo(currentMonth.year, currentMonth.monthValue)
                            val targetDate = selectedDate ?: todayDate
                            selectedDate = targetDate
                            dataBinding.icCalendar.calendarView.notifyDateChanged(targetDate)
                            viewModel.getDayInfo(targetDate.year, targetDate.monthValue, targetDate.dayOfMonth)
                            dataBinding.calendarBottomSheet.post {
                                updateBottomSheetLayout()
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            }
                        } else {
                            clearCalendarBottomSheet()
                            dataBinding.tvCalendarEmpty.visibility = android.view.View.GONE
                        }
                    }
                }

                launch {
                    viewModel.monthDeliveryDates.collectLatest {
                        dataBinding.icCalendar.calendarView.notifyCalendarChanged()
                    }
                }

                launch {
                    viewModel.calendarDayDeliveries.collectLatest { dayDeliveries ->
                        if (viewModel.isShowCalendar.value && dayDeliveries != null) {
                            calendarBottomSheetAdapter.submitData(PagingData.from(dayDeliveries))
                            dataBinding.tvCalendarEmpty.visibility =
                                if (dayDeliveries.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                        }
                    }
                }
            }
        }
    }

    inner class DayViewContainer(view: android.view.View) : ViewContainer(view) {
        val binding = CalendarDayLayoutBinding.bind(view)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position != DayPosition.MonthDate) {
                    return@setOnClickListener
                }

                val previousSelection = selectedDate
                if (previousSelection == day.date) {
                    return@setOnClickListener
                }

                selectedDate = day.date
                dataBinding.icCalendar.calendarView.notifyDateChanged(day.date)
                viewModel.getDayInfo(day.date.year, day.date.monthValue, day.date.dayOfMonth)
                previousSelection?.let { dataBinding.icCalendar.calendarView.notifyDateChanged(it) }
            }
        }
    }

    inner class MonthViewContainer(view: android.view.View) : ViewContainer(view) {
        val binding = CalendarDayTitleContainerBinding.bind(view)
        val titlesContainer = binding.root
    }

    private fun updateBottomSheetLayout() {
        if (!viewModel.isShowCalendar.value) return

        val parentHeight = (dataBinding.calendarBottomSheet.parent as? android.view.View)?.height ?: return
        val headerBottom = dataBinding.icCalendar.calendarHeaderContainer.bottom
        val calendarBottom = dataBinding.icCalendar.calendarContentContainer.bottom
        if (parentHeight == 0 || headerBottom == 0 || calendarBottom == 0) return

        bottomSheetBehavior.expandedOffset = headerBottom
        val peekTop = (calendarBottom - 8.dpToPx()).coerceAtLeast(headerBottom)
        bottomSheetBehavior.peekHeight = (parentHeight - peekTop).coerceAtLeast(240.dpToPx())
        bottomSheetBehavior.halfExpandedRatio =
            1f - (bottomSheetBehavior.peekHeight.toFloat() / parentHeight.toFloat())
        bottomSheetExpandedTop = headerBottom
        bottomSheetCollapsedTop = parentHeight - bottomSheetBehavior.peekHeight
    }

    private fun snapBottomSheet(currentTop: Int) {
        if (!::bottomSheetBehavior.isInitialized) return
        if (bottomSheetExpandedTop == 0 && bottomSheetCollapsedTop == 0) return

        val targetState = if (
            kotlin.math.abs(currentTop - bottomSheetExpandedTop) <=
            kotlin.math.abs(currentTop - bottomSheetCollapsedTop)
        ) {
            BottomSheetBehavior.STATE_EXPANDED
        } else {
            BottomSheetBehavior.STATE_COLLAPSED
        }

        if (bottomSheetBehavior.state != targetState) {
            bottomSheetBehavior.state = targetState
        }
    }

    private fun bindDeliveryDots(
        binding: CalendarDayLayoutBinding,
        dots: Set<DeliveryDotType>
    ) {
        binding.llDeliveryDots.visibility =
            if (dots.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
        binding.vDeliveryDotRed.visibility =
            if (dots.contains(DeliveryDotType.RED)) android.view.View.VISIBLE else android.view.View.GONE
        binding.vDeliveryDotGreen.visibility =
            if (dots.contains(DeliveryDotType.GREEN)) android.view.View.VISIBLE else android.view.View.GONE
        binding.vDeliveryDotGray.visibility =
            if (dots.contains(DeliveryDotType.GRAY)) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun getDeliveryDots(
        date: LocalDate,
        isMonthDate: Boolean
    ): Set<DeliveryDotType> {
        if (!isMonthDate) return emptySet()
        if (!viewModel.monthDeliveryDates.value.contains(date)) return emptySet()
        return setOf(
            when {
                date < todayDate -> DeliveryDotType.GRAY
                date == todayDate -> DeliveryDotType.GREEN
                else -> DeliveryDotType.RED
            }
        )
    }

    private fun clearCalendarBottomSheet() {
        if (::calendarBottomSheetAdapter.isInitialized) {
            lifecycleScope.launch {
                calendarBottomSheetAdapter.submitData(PagingData.empty())
            }
        }
    }

    private enum class DeliveryDotType {
        RED,
        GREEN,
        GRAY
    }
}
