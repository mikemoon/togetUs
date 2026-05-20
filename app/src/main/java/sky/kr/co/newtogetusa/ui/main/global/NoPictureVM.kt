package sky.kr.co.newtogetusa.ui.main.global

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryUploadPictureRequest
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class NoPictureVM @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository
) :
    BaseViewModel(baseViewModelFactory.create()) {

    val selectedReasonIndex = MutableLiveData(1)
    val reasonDetail = MutableLiveData("")
    val reasonLength = MutableLiveData(0)

    fun selectReason(index: Int) {
        selectedReasonIndex.value = index
    }

    fun updateReasonDetail(text: String) {
        val trimmed = if (text.length > MAX_REASON_LENGTH) {
            text.take(MAX_REASON_LENGTH)
        } else {
            text
        }
        reasonDetail.value = trimmed
        reasonLength.value = trimmed.length
    }

    fun requestCompleteWithoutPicture(deliveryId: Long, proofType: String) = viewModelScope.launch {
        if (deliveryId <= 0L) {
            _event.value = Event.InvalidDeliveryId
            return@launch
        }

        loadingState.value = true
        val request = DeliveryUploadPictureRequest(
                mime = "",
                base64 = "",
                latitude = 0.0,
                longitude = 0.0,
                picture_date = "",
                no_picture_cd = selectedReasonCode(),
                no_picture_reason = reasonDetail.value.orEmpty()
        )
        val res = if (proofType == TakePhotoVM.PROOF_PICKUP) {
            deliveryRepository.putPickupDonePicture(deliveryId, request)
        } else {
            deliveryRepository.putDeliveryCompletePicture(deliveryId, request)
        }

        when (res) {
            is ResultWrapper.Success -> _event.value = Event.CompleteSuccess
            is ResultWrapper.GenericError,
            is ResultWrapper.NetworkError -> _event.value = Event.CompleteFailed
        }
        loadingState.value = false
    }

    private fun selectedReasonCode(): String {
        return when (selectedReasonIndex.value ?: 1) {
            0 -> "DIRECT_DELIVERY"
            1 -> "PHOTO_PROHIBITED"
            2 -> "TOO_DARK"
            3 -> "NETWORK_ERROR"
            4 -> "OTHER"
            else -> "PHOTO_PROHIBITED"
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    sealed class Event {
        object CompleteSuccess : Event()
        object CompleteFailed : Event()
        object InvalidDeliveryId : Event()
    }

    companion object {
        const val MAX_REASON_LENGTH = 30
    }
}
