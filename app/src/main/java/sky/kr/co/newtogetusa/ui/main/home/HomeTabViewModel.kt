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
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
                                           private val configRepository: ConfigRepository,
                                           private val kakaoRepo: KakaoLocalRepository,
                                           private val deliveryRepo : DeliveryRepository) : BaseViewModel(baseViewModelFactory.create()) {

    val isModePlayer = MutableStateFlow(false)
    val mapShowState = MutableStateFlow<MapShow>(MapShow.LOCAL_IMAGE)

    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull().collectLatest {
                isModePlayer.value = it
            }
        }
        viewModelScope.launch {
            configRepository.getDomesticAreas()
        }

        decideMapProvider()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object JoinPlayer : Event()
        object RequestDelivery : Event()
    }

    /**
     * 📌 현재 국가에 따라 지도 결정
     */
    private fun decideMapProvider() {
        val isKorea = Locale.getDefault().country.equals("KR", ignoreCase = true)

        mapShowState.value = if (isKorea) {
            MapShow.KAKAO_MAP
        } else {
            MapShow.GOOGLE_MAP
        }
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