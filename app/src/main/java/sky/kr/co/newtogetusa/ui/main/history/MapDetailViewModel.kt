package sky.kr.co.newtogetusa.ui.main.history

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapDetailViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val mapShowState = MutableStateFlow(HomeTabViewModel.MapShow.KAKAO_MAP)

    // iOS DeliveryMapViewModel needsPlayerTracking 대응
    data class PlayerGps(
        val latitude: Double,
        val longitude: Double,
        val nickname: String
    )

    val needsPlayerTracking = MutableStateFlow(false)
    val playerGps = MutableStateFlow<PlayerGps?>(null)

    /** refresh 버튼 탭 시 true → 다음 GPS 응답에서 fitMapToAllPoints 수행 */
    val fitOnNextGps = MutableStateFlow(false)

    private var deliveryId: Long = -1L
    private var trackingJob: Job? = null

    init {
        viewModelScope.launch {
            val isInternational = dataStoreRepository.getBoolean(DataStoreKey.KEY_ABROAD_DELIVERY_AGREE) ?: false
            val isKakaoMapAvailable = sky.kr.co.newtogetusa.utils.KakaoMapSupport.isAvailable
            mapShowState.value = when {
                isInternational -> HomeTabViewModel.MapShow.GOOGLE_MAP
                !isKakaoMapAvailable -> HomeTabViewModel.MapShow.GOOGLE_MAP
                else -> HomeTabViewModel.MapShow.KAKAO_MAP
            }
        }
    }

    // iOS: DELIVERY_START / DELIVERY_ING 상태일 때만 플레이어 추적
    fun initTracking(deliveryId: Long, statusCd: String?) {
        this.deliveryId = deliveryId
        val needsTracking = statusCd == "DELIVERY_START" || statusCd == "DELIVERY_ING"
        needsPlayerTracking.value = needsTracking
        if (needsTracking && deliveryId > 0) {
            fitOnNextGps.value = true // iOS: 최초 GPS 수신 시 fitMapToAllPoints
            startPlayerTracking()
        }
    }

    // iOS: 즉시 1회 조회 후 60초 간격 폴링
    private fun startPlayerTracking() {
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            while (true) {
                fetchPlayerGps()
                delay(60_000L)
            }
        }
    }

    // iOS refreshPlayerGps 대응
    fun refreshPlayerGps() {
        fitOnNextGps.value = true
        viewModelScope.launch { fetchPlayerGps() }
    }

    private suspend fun fetchPlayerGps() {
        if (deliveryId <= 0) return
        when (val result = deliveryRepository.getPlayerGps(deliveryId)) {
            is ResultWrapper.Success -> {
                val member = result.data.members.firstOrNull {
                    it.gps_latitude != 0.0 && it.gps_longitude != 0.0
                } ?: return
                playerGps.value = PlayerGps(member.gps_latitude, member.gps_longitude, member.nickname)
            }
            else -> Timber.w("getPlayerGps failed: $result")
        }
    }

    // iOS viewWillDisappear 에서 추적 중단 대응
    fun stopPlayerTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }
}
