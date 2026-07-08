package sky.kr.co.newtogetusa.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySearchResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.data.remote.dto.users.BannerDto
import sky.kr.co.newtogetusa.data.remote.dto.users.BannerLandingDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
                                           private val configRepository: ConfigRepository,
                                           private val kakaoRepo: KakaoLocalRepository,
                                           private val userRepository: UserRepository,
                                           private val deliveryRepo : DeliveryRepository,
                                           private val myRepository: MyRepository,
                                           private val playerRepository: PlayerRepository) : BaseViewModel(baseViewModelFactory.create()) {

    val isModePlayer = MutableStateFlow(false)
    val mapShowState = MutableStateFlow<MapShow>(MapShow.GOOGLE_MAP)
    val unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationText = MutableStateFlow("")
    val locationAlarmOn = MutableStateFlow(false)
    private var hasHomeRefreshStarted = false

    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull().collectLatest {
                val isChanged = isModePlayer.value != it
                isModePlayer.value = it
                if (isChanged && hasHomeRefreshStarted) {
                    refreshHome()
                }
            }
        }
        viewModelScope.launch {
            configRepository.getDomesticAreas()
        }

        getNotificationUnreadCount()
    }

    fun getNotificationUnreadCount() = viewModelScope.launch {
        when (val res = myRepository.getNotificationUnreadCount()) {
            is ResultWrapper.Success -> {
                val count = res.data.unreadCount
                unreadNotificationCount.value = count
                unreadNotificationText.value = if (count > 99) "99+" else count.toString()
            }
            else -> {
                unreadNotificationCount.value = 0
                unreadNotificationText.value = ""
            }
        }
    }

    val doingDeliveryList = MutableStateFlow<List<DeliverySummaryDto>?>(null)
    val registeredDeliveryList = MutableStateFlow<List<DeliverySummaryDto>?>(null)

    fun postDeliverySearch(deliverySearchReq: DeliverySearchReq) = viewModelScope.launch {
        val res = deliveryRepo.postDeliverySearch(deliverySearchReq)
        when(res){
            is ResultWrapper.Success ->{
                val matchList = res.data.deliveries.filter {
                    it.status_cd.startsWith("MATCH")
                }

                val deliveryList = res.data.deliveries.filter {
                    it.status_cd.startsWith("DELIVERY")
                }

                registeredDeliveryList.value = matchList.map { it.apply { setUiValue() }}
                doingDeliveryList.value = deliveryList.map { it.apply { setUiValue() }}
            }
            else -> {}
        }
    }

    val doingPlayerDeliveryList = MutableStateFlow<List<DeliverySummaryDto>?>(null)
    val applyDeliveryList = MutableStateFlow<List<DeliverySummaryDto>?>(null)
    val availableDeliveryList = MutableStateFlow<List<DeliverySummaryDto>?>(null)
    val doingPlayerDeliveryHasMore = MutableStateFlow(false)
    val applyDeliveryHasMore = MutableStateFlow(false)

    private fun postPlayerDeliverySearch(
        type: String,
        onSuccess: (List<DeliverySummaryDto>, Boolean) -> Unit
    ) = viewModelScope.launch {
        val deliverySearchReq = DeliverySearchReq(
            type = type,
            title = "",
            page_no = 0
        )
        val res = deliveryRepo.postPlayerDeliverySearch(deliverySearchReq)
        when(res){
            is ResultWrapper.Success ->{
                val myUserId = if (type == "ENABLE") {
                    dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)?.user?.user_id?.toLong()
                } else {
                    null
                }
                val deliveries = res.data.deliveries
                    .filter { myUserId == null || it.requester_id != myUserId }
                    .map { it.apply { setUiValue() } }
                onSuccess(deliveries, res.data.has_more)
            }
            is ResultWrapper.NetworkError ->{
                onSuccess(emptyList(), false)
            }
            is ResultWrapper.GenericError ->{
                onSuccess(emptyList(), false)
            }
        }
    }

    fun refreshHome() = viewModelScope.launch {
        hasHomeRefreshStarted = true
        val isPlayerMode = dataStoreRepository.getBoolean(DataStoreKey.KEY_IS_MODE_PLAYER) ?: false
        isModePlayer.value = isPlayerMode

        val request = DeliverySearchReq(
            type = "DELIVERY|MATCH",
            title = "",
            page_no = 0
        )
        if (isPlayerMode) {
            doingDeliveryList.value = emptyList()
            registeredDeliveryList.value = emptyList()
            syncLocationAlarmState()
            postPlayerDeliverySearch("DELIVERY") { list, hasMore ->
                doingPlayerDeliveryHasMore.value = hasMore
                doingPlayerDeliveryList.value = list
            }
            postPlayerDeliverySearch("MATCH") { list, hasMore ->
                applyDeliveryHasMore.value = hasMore
                applyDeliveryList.value = list
            }
            postPlayerDeliverySearch("ENABLE") { list, _ ->
                availableDeliveryList.value = list
            }
        } else {
            doingPlayerDeliveryList.value = emptyList()
            applyDeliveryList.value = emptyList()
            availableDeliveryList.value = emptyList()
            doingPlayerDeliveryHasMore.value = false
            applyDeliveryHasMore.value = false
            postDeliverySearch(request)
        }
        getBanners()
        getNotificationUnreadCount()
    }

    private fun syncLocationAlarmState() = viewModelScope.launch {
        val playerId = resolvePlayerId() ?: return@launch

        when (val res = playerRepository.getProfile(playerId)) {
            is ResultWrapper.Success -> locationAlarmOn.value = res.data.gps_area
            else -> Unit
        }
    }

    fun toggleLocationAlarm() = viewModelScope.launch {
        if (loadingState.value) return@launch

        val playerId = resolvePlayerId()
        if (playerId == null) {
            _event.value = Event.LocationAlarmFailed
            return@launch
        }

        val next = !locationAlarmOn.value
        loadingState.value = true
        try {
            when (val res = playerRepository.setPlayerGpsEnable(playerId, next)) {
                is ResultWrapper.Success -> {
                    if (res.data) {
                        locationAlarmOn.value = next
                        loadingState.value = false
                        _event.value = Event.LocationAlarmChanged(next)
                    } else {
                        loadingState.value = false
                        _event.value = Event.LocationAlarmFailed
                    }
                }
                else -> {
                    syncLocationAlarmState()
                    loadingState.value = false
                    _event.value = Event.LocationAlarmFailed
                }
            }
        } finally {
            loadingState.value = false
        }
    }

    fun refreshLocationAlarm(latitude: Double, longitude: Double) = viewModelScope.launch {
        val playerId = resolvePlayerId() ?: return@launch
        playerRepository.refreshPlayerGps(playerId, latitude, longitude)
    }

    private suspend fun resolvePlayerId(): Int? {
        dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)
            ?.user
            ?.player_id
            ?.takeIf { it > 0 }
            ?.let { return it }

        return when (val response = userRepository.getMyProfile()) {
            is ResultWrapper.Success -> {
                dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                response.data.user.player_id.takeIf { it > 0 }
            }
            else -> null
        }
    }

    val bannerList = MutableStateFlow<List<BannerDto>?>(null)
    fun getBanners() = viewModelScope.launch {
        when(val res = userRepository.getBanners()){
            is ResultWrapper.Success ->{
                bannerList.value = res.data
            }
            else -> {}
        }
    }

    val bannerDetail = MutableStateFlow<BannerLandingDto?>(null)
    fun getBannerDetail(bnId: Int) = viewModelScope.launch {
        when(val res = userRepository.getBannerDetail(bnId)){
            is ResultWrapper.Success ->{
                bannerDetail.value = res.data
            }
            else -> {}
        }
    }

    fun clearBannerDetail() {
        bannerDetail.value = null
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object JoinPlayer : Event()
        object RequestDelivery : Event()
        object Alarm : Event()
        data class LocationAlarmChanged(val isOn: Boolean) : Event()
        object LocationAlarmFailed : Event()
    }

    private val _address = MutableStateFlow<String?>("현재위치")
    val address: StateFlow<String?> = _address

    fun fetchAddress(lat: Double, lng: Double) {
        viewModelScope.launch {
            runCatching { kakaoRepo.getAddressFromCoord(lat, lng) }
                .onSuccess { _address.value = it?.roadAddress }
                .onFailure { _address.value = null }
        }
    }

    enum class MapShow {
        LOCAL_IMAGE,
        KAKAO_MAP,
        GOOGLE_MAP
    }
}
