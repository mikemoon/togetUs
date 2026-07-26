package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.player.DeliverySummaryDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerDeliveryHistoryReq
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.history.HistoryViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryDeliveryViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                   private val playerRepository: PlayerRepository,
                                                   private val deliveryRepo : DeliveryRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){


    val isModePlayer = MutableStateFlow<Boolean?>(null)
    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                .collectLatest { isPlayerMode ->
                    isModePlayer.value = isPlayerMode
                }
        }
    }

    private val _topMenu = MutableStateFlow("ALL")
    private val _keyword = MutableStateFlow("")

    private val _itemCancelLiveData = SingleLiveEvent<DeliverySummaryDto>()
    val itemCancelLiveData: LiveData<DeliverySummaryDto> = _itemCancelLiveData
    fun onItemCancel(item:DeliverySummaryDto){
        _itemCancelLiveData.value = item
    }

    val menuAll = TopMenu.All
    val menuDoing = TopMenu.Doing
    val menuEnd = TopMenu.End

    private val _topMenuLiveData = SingleLiveEvent<TopMenu>()
    val topMenuLiveData: LiveData<TopMenu> = _topMenuLiveData

    fun onTopMenuSelect(topMenu: TopMenu) {
        _topMenu.value = when(topMenu){
            is TopMenu.All -> "ALL"
            is TopMenu.Doing -> "ING"//"REG|MATCH|DELIVERY|ING"
            is TopMenu.End -> "DONE"
        }
        _topMenuLiveData.value = topMenu
    }

    val isShowCalendar = MutableStateFlow(false)
    fun onShowCalendar(isShow: Boolean){
        isShowCalendar.value = isShow
    }

    private val _monthDeliveryDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val monthDeliveryDates = _monthDeliveryDates.asStateFlow()

    private val _calendarDayDeliveries = MutableStateFlow<List<DeliverySummaryDto>?>(null)
    val calendarDayDeliveries = _calendarDayDeliveries.asStateFlow()

    // iOS 대응: 월별 전체 배송 데이터 (dot 생성 및 날짜 필터링용)
    private val _calendarDeliveries = MutableStateFlow<List<DeliverySummaryDto>>(emptyList())
    val calendarDeliveries = _calendarDeliveries.asStateFlow()

    // iOS 대응: 날짜별 dot 데이터 (key: LocalDate, value: DotType)
    private val _calendarDotData = MutableStateFlow<Map<LocalDate, DeliveryDotType>>(emptyMap())
    val calendarDotData = _calendarDotData.asStateFlow()

    enum class DeliveryDotType {
        GRAY,   // 매칭 진행중
        GREEN,  // 동행 대기중 ~ 동행중
        NAVY    // 동행 완료
    }

    fun getMonthInfo(year: Int, month: Int) = viewModelScope.launch {
        when(val res = playerRepository.getMonthInfo(year, month)) {
            is ResultWrapper.Success -> {
                _monthDeliveryDates.value = res.data.calender.mapNotNull { dayInfo ->
                    dayInfo.date.takeIf { it.isNotBlank() }?.runCatching { LocalDate.parse(this) }?.getOrNull()
                }.filter { it.monthValue == month && it.year == year }
                    .toSet()

                // iOS 대응: 월별 전체 배송 데이터로 dot 데이터 생성
                val allDeliveries = res.data.calender.flatMap { dayInfo ->
                    dayInfo.deliveries.map { delivery ->
                        DeliverySummaryDto(
                            deliveryId = delivery.deliveryId,
                            requesterId = 0,
                            playerId = null,
                            title = "",
                            statusCd = delivery.status,
                            prdPicture = null,
                            departAddress = "",
                            destAddress = "",
                            pickupImmediately = false,
                            pickupDate = delivery.date,
                            feeFinal = 0,
                            registDate = null,
                            applyDate = null
                        )
                    }
                }
                _calendarDeliveries.value = allDeliveries
                buildDotData(allDeliveries)
            }
            else -> {
                _monthDeliveryDates.value = emptySet()
                _calendarDeliveries.value = emptyList()
                _calendarDotData.value = emptyMap()
            }
        }
    }

    fun getDayInfo(year: Int, month: Int, day: Int) = viewModelScope.launch {
        when (val res = playerRepository.getDayInfo(year, month, day)) {
            is ResultWrapper.Success -> {
                _calendarDayDeliveries.value = res.data.deliveries
            }
            else -> {
                _calendarDayDeliveries.value = emptyList()
            }
        }
    }

    // iOS 대응: 월별 전체 배송 데이터로 dot 데이터 생성
    fun buildDotData(deliveries: List<DeliverySummaryDto>) {
        val dotMap = mutableMapOf<LocalDate, DeliveryDotType>()
        deliveries.forEach { delivery ->
            val dateKey = delivery.pickupDate.take(8) // "yyyyMMdd"
            if (dateKey.length == 8) {
                runCatching {
                    val date = LocalDate.parse(dateKey, java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                    val dotType = getDotType(delivery.statusCd)
                    val existing = dotMap[date]
                    dotMap[date] = if (existing != null) higherPriority(existing, dotType) else dotType
                }
            }
        }
        _calendarDotData.value = dotMap
    }

    // iOS 대응: 선택된 날짜의 배송 목록 필터링
    fun filterDeliveriesForDate(date: LocalDate) {
        val dateKey = date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
        _calendarDayDeliveries.value = _calendarDeliveries.value.filter { delivery ->
            delivery.pickupDate.take(8) == dateKey
        }
    }

    private fun getDotType(statusCd: String): DeliveryDotType {
        return when (statusCd) {
            "MATCH_BEFORE", "MATCH_ING" -> DeliveryDotType.GRAY
            "DELIVERY_BEFORE", "DELIVERY_START", "DELIVERY_ING" -> DeliveryDotType.GREEN
            "DELIVERY_END" -> DeliveryDotType.NAVY
            else -> DeliveryDotType.GRAY
        }
    }

    private fun higherPriority(a: DeliveryDotType, b: DeliveryDotType): DeliveryDotType {
        val priority = mapOf(
            DeliveryDotType.GREEN to 3,
            DeliveryDotType.NAVY to 2,
            DeliveryDotType.GRAY to 1
        )
        return if ((priority[a] ?: 0) >= (priority[b] ?: 0)) a else b
    }


    fun getPlayerDeliveryList(request: PlayerDeliveryHistoryReq) = viewModelScope.launch {
        when(val res = playerRepository.postPlayerDeliveryList(request)){
            is ResultWrapper.Success -> {

            }
            else ->{}
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val deliveryPagingFlow =
        combine(_topMenu, _keyword) { type, keyword ->
            type to keyword
        }.flatMapLatest { (type, keyword) ->
            playerRepository.getPlayerDeliveryHistoryPagingFlow(type, keyword)
        }.cachedIn(viewModelScope)

    sealed class TopMenu {
        object All : TopMenu()
        object Doing : TopMenu()
        object End : TopMenu()
    }
}