package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.dto.DirectionsResponse
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryMapViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                               private val directionsRepository: DirectionsRepository,
                                               private val kakaoRepo: KakaoLocalRepository
    ):BaseViewModel(baseViewModelDependenciesFactory.create()) {

        val isInternationalDelivery = MutableStateFlow(false)

    private val _route = MutableLiveData<List<LatLng>>()
    val route: LiveData<List<LatLng>> = _route

    fun fetchRoute(origin: LatLng, destination: LatLng, waypoints: List<LatLng> = emptyList()) {
        viewModelScope.launch {
            Timber.d("RouteFetch origin: $origin , destination :  $destination")
            val result = directionsRepository.getRoutePoints(origin, destination, waypoints, "AIzaSyA9ZdMta--H5FgxapDt2AyHV6ZooYBht54")
            result.onSuccess {
                _route.postValue(it)
            }.onFailure {
                Timber.e("RouteFetch Error: ${it.message}")
            }
        }
    }

    private val _routeByPlaceIds = SingleLiveEvent<DirectionsResponse>()
    val routeByPlaceIds: LiveData<DirectionsResponse> = _routeByPlaceIds
    fun getRouteByPlaceIds(originPlaceId: String, destinationPlaceId: String, waypointsPlaceIds: List<String>? = null) {
        viewModelScope.launch {
            Timber.d("RouteFetch originPlaceId: $originPlaceId , destinationPlaceId :  $destinationPlaceId")
            val response = directionsRepository.getRouteByPlaceIds(originPlaceId, destinationPlaceId, waypointsPlaceIds)
            Timber.d("RouteFetch Response: $response")
            _routeByPlaceIds.value = response
        }
    }

    private val _latLng = MutableLiveData<LatLng>()
    val latLng: LiveData<LatLng> = _latLng

    fun fetchLatLng(placeId: String, callback: (LatLng?) -> Unit) {
        viewModelScope.launch {
            val result = directionsRepository.getLatLngByPlaceId(placeId)
            Timber.d("fetchLatLng result: $result")
            result?.let {
                _latLng.value = it
                callback(it)
            }
        }
    }

    val name = MutableStateFlow("")
    val phone = MutableStateFlow("")

    //카카오 주소 얻기
    private val _destinationAddress = MutableStateFlow<String?>("도착지 선택")
    val destinationAddress: StateFlow<String?> = _destinationAddress

    val destinationDetailAddress = MutableStateFlow("")

    fun setDestinationAddress(address: String) {
        _destinationAddress.value = address
    }

    private val _address = MutableStateFlow<String?>("출발지 선택")
    val address: StateFlow<String?> = _address

    val addressDetail = MutableStateFlow("")

    fun setStartAddress(address: String) {
        _address.value = address
    }

    val confirmButtonEnable = MutableStateFlow(false)
    fun updateConfirmButtonEnable(enable: Boolean) {
        confirmButtonEnable.value = enable
    }

    fun fetchAddress(lat: Double, lng: Double) {
        viewModelScope.launch {
            runCatching { kakaoRepo.getAddressFromCoord(lat, lng) }
                .onSuccess {
                    val address = it?.roadAddress?.takeIf { roadAddress -> roadAddress.isNotBlank() }
                        ?: it?.name?.takeIf { name -> name.isNotBlank() }
                        ?: it?.subtitle?.takeIf { subtitle -> subtitle.isNotBlank() }
                    if (!address.isNullOrBlank()) {
                        _address.value = address
                    }
                }
                .onFailure {
                    Timber.e(it, "fetchAddress failed")
                }
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object SelectStart : Event()
        object SelectDestination : Event()

        object Confirm : Event()
    }
}
