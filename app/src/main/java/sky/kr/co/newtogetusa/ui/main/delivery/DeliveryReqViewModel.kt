package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryReqViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository
) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isInternationalDelivery = MutableStateFlow(false)
    val isAgreeAbroadDelivery = MutableStateFlow(false) //해외배송안내 동의

    val chargeShowButtonEnable = MutableStateFlow(false)
    fun updateChargeEnable(enable: Boolean) {
        chargeShowButtonEnable.value = enable
    }

    fun onAbroadDeliveryClick(callback: (isAgree: Boolean) -> Unit) = viewModelScope.launch {
        Timber.d("isAgreeAbroadDelivery : ${isAgreeAbroadDelivery.value}")
        if (!isAgreeAbroadDelivery.value) {
            isAgreeAbroadDelivery.value =
                dataStoreRepository.getBoolean(DataStoreKey.KEY_ABROAD_DELIVERY_AGREE) == true
        }
        callback.invoke(isAgreeAbroadDelivery.value)
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    fun editDelivery(deliveryId: Long, deliveryRequest: DeliveryRequest) = viewModelScope.launch {
        loadingState.value = true
        val event = try {
            when (val res = deliveryRepository.editDelivery(deliveryId, deliveryRequest)) {
                is ResultWrapper.Success -> Event.EditCompleted
                is ResultWrapper.GenericError -> Event.ShowMessage(
                    res.message?.takeIf { it.isNotBlank() }
                        ?: "동행요청 정보를 수정할수 없는 단계입니다"
                )
                is ResultWrapper.NetworkError -> Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
        } catch (error: Exception) {
            Timber.e(error, "Failed to edit delivery")
            Event.ShowMessage("동행요청 정보를 수정할수 없는 단계입니다")
        } finally {
            // 오류 안내 팝업을 열기 전에 로딩을 해제해 팝업을 닫아도 로딩이 남지 않게 한다.
            loadingState.value = false
        }
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object StartLocation : Event()
        object PickupDate : Event()
        object ProductInfo : Event()
        object Charge : Event()
        object EditCompleted : Event()
        data class ShowMessage(val message: String) : Event()
    }
}
