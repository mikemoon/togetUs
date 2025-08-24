package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryAddressMapViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                      private val directionsRepository: DirectionsRepository,
                                                      private val kakaoRepo: KakaoLocalRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _selectedAddress = MutableStateFlow<KakaoSearchModel?>(null)
    val selectedAddress: StateFlow<KakaoSearchModel?> = _selectedAddress

    fun reverseGeocode(lat: Double, lng: Double) = viewModelScope.launch {
        // ⚠️ Kakao Local은 x=lng, y=lat
        runCatching { kakaoRepo.getAddressFromCoord(lng = lng, lat = lat) }
            .onSuccess { res ->
                _selectedAddress.value = res
            }
            .onFailure { _selectedAddress.value = null }
    }

    private val _address = MutableStateFlow<String?>("출발지 선택")
    val address: StateFlow<String?> = _address

    fun setStartAddress(address: String) {
        _address.value = address
    }

    fun fetchAddress(lat: Double, lng: Double) {
        viewModelScope.launch {
            runCatching { kakaoRepo.getAddressFromCoord(lat, lng) }
                .onSuccess { _address.value = it?.roadAddress
                    _selectedAddress.value = it }
                .onFailure { _address.value = null }
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object SetAddress : Event()
    }
}