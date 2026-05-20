package sky.kr.co.newtogetusa.ui.main.global

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryUploadPictureRequest
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.utils.bitmapToBase64
import android.graphics.BitmapFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TakePhotoVM @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository,
) :
    BaseViewModel(baseViewModelFactory.create()) {

    data class CapturedPhotoMeta(
        val file: File,
        val latitude: Double?,
        val longitude: Double?,
        val address: String?
    )

    val showRetakeButtons = MutableLiveData(false)
    val capturedPhotoMeta = MutableLiveData<CapturedPhotoMeta?>(null)
    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun setRetakeMode(enable: Boolean) {
        showRetakeButtons.value = enable
    }

    fun onPhotoCaptured(file: File, latitude: Double?, longitude: Double?, address: String?) {
        capturedPhotoMeta.value = CapturedPhotoMeta(
            file = file,
            latitude = latitude,
            longitude = longitude,
            address = address
        )
        showRetakeButtons.value = true
    }

    fun uploadCapturedPhoto(deliveryId: Long, proofType: String) = viewModelScope.launch {
        val meta = capturedPhotoMeta.value
        if (deliveryId <= 0L || meta == null) {
            _event.value = Event.Invalid
            return@launch
        }

        val bitmap = BitmapFactory.decodeFile(meta.file.absolutePath)
        val request = DeliveryUploadPictureRequest(
            mime = "image/jpeg",
            base64 = bitmapToBase64(bitmap),
            latitude = meta.latitude ?: 0.0,
            longitude = meta.longitude ?: 0.0,
            picture_date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            no_picture_cd = "",
            no_picture_reason = ""
        )

        loadingState.value = true
        val res = if (proofType == PROOF_PICKUP) {
            deliveryRepository.putPickupDonePicture(deliveryId, request)
        } else {
            deliveryRepository.putDeliveryCompletePicture(deliveryId, request)
        }
        _event.value = if (res is ResultWrapper.Success) Event.UploadSuccess else Event.UploadFailed
        loadingState.value = false
    }

    sealed class Event {
        object UploadSuccess : Event()
        object UploadFailed : Event()
        object Invalid : Event()
    }

    companion object {
        const val PROOF_PICKUP = "pickup"
        const val PROOF_COMPLETE = "complete"
    }
}
