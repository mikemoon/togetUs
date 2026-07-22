package sky.kr.co.newtogetusa.ui.main.delivery

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRegPhoto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryFeeVM @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository
    ):
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

        val isAgreeChecked = MutableStateFlow(false)
        val registerButtonEnabled = MutableStateFlow(false)

    private val deliveryIdFlow = MutableStateFlow<Long?>(null)

    private val distanceKm = MutableStateFlow<String?>(null)
    private val productWeight = MutableStateFlow<String?>(null)
    private val feeValue = MutableStateFlow<Long?>(null)
    private val adjustFeeValue = MutableStateFlow<Long?>(null)

    fun setDistance(distance: String) {
        distanceKm.value = distance
    }

    fun setProductWeight(weight: String?) {
        productWeight.value = weight
    }

    fun createDelivery(deliveryReq: DeliveryRequest, result: (Long) -> Unit) = viewModelScope.launch {
        loadingState.value = true
        val res = deliveryRepository.postDelivery(deliveryReq)
        when(res){
            is ResultWrapper.Success -> {
                deliveryIdFlow.value = res.data.deliveryId
                result.invoke(res.data.deliveryId)
            }
            is ResultWrapper.GenericError ->{
            }
            is ResultWrapper.NetworkError ->{
            }
        }
    }
    fun checkingFee(deliveryId: Long) = viewModelScope.launch {
        val res = deliveryRepository.getDeliveryFee(deliveryId)
        when(res){
            is ResultWrapper.Success -> {
                feeValue.value = res.data.feeBasic.toLong()
                adjustFeeValue.value = res.data.feeAdjust.toLong()
            }
            is ResultWrapper.GenericError ->{
            }
            is ResultWrapper.NetworkError ->{
            }
        }
        loadingState.value = false
    }

    fun loadDeliveryFeeForEdit(deliveryId: Long) = viewModelScope.launch {
        deliveryIdFlow.value = deliveryId
        loadingState.value = true
        when (val res = deliveryRepository.getDeliveryFee(deliveryId)) {
            is ResultWrapper.Success -> {
                feeValue.value = res.data.feeBasic.toLong()
                adjustFeeValue.value = res.data.feeAdjust.toLong()
                distanceKm.value = "${res.data.expectedStraight} km"
                productWeight.value = weightLabel(res.data.expectedWeightCd)
            }
            else -> Unit
        }
        loadingState.value = false
    }

    fun registerPhoto(photos: List<DeliveryRegPhoto>, resultCallback: (Boolean) -> Unit) = viewModelScope.launch {
        loadingState.value = true
        val res = if (photos.size == 1) {
            deliveryRepository.postDeliveryPicture(deliveryIdFlow.value?:return@launch,photos.first())
        } else {
            deliveryRepository.postDeliveryPictures(deliveryIdFlow.value?:return@launch, photos)
        }

        when (res) {
            is ResultWrapper.Success -> resultCallback(true)
            else -> resultCallback(false)
        }
        loadingState.value = false
    }

    fun registerDelivery(req: DeliveryFinalReq, result: (Boolean) -> Unit) = viewModelScope.launch {
        val res = deliveryRepository.putDeliveryFinalReq(deliveryIdFlow.value?:return@launch, req)
        when(res){
            is ResultWrapper.Success -> {
                result.invoke(res.data)
            }
            is ResultWrapper.GenericError ->{
            }
            is ResultWrapper.NetworkError ->{
            }
        }
    }

    val deliveryUiModel = combine(
        distanceKm,
        productWeight,
        feeValue,
        adjustFeeValue
    ) { distance, weight, fee, adjustFee ->

        if (distance == null || weight == null || fee == null) {
            null
        } else {
            DeliveryFeeUiModel(
                distance = distance,
                weight = weight,
                fee = "%,d원".format(fee),
                baseFee = fee,
                // A zero additional fee is represented by the input hint, not as entered text.
                adjustFee = adjustFee?.takeIf { it > 0L }?.toString().orEmpty()
            )
        }
    }

    private fun weightLabel(code: String): String = when (code.lowercase()) {
        "light", "small" -> "가벼움(~3kg)"
        "medium" -> "보통(3~10kg)"
        else -> code
    }

    fun onAgree(){
        isAgreeChecked.value = !isAgreeChecked.value
        registerButtonEnabled.value = isAgreeChecked.value
    }

    fun setRegisterButtonEnabled(enabled: Boolean) {
        registerButtonEnabled.value = enabled
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object RegisterDelivery : Event()
    }
}

data class DeliveryFeeUiModel(
    val distance: String,
    val weight: String,
    val fee: String,
    val baseFee: Long,
    val adjustFee: String
)
