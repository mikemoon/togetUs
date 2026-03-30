package sky.kr.co.newtogetusa.ui.main.global

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TakePhotoVM @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory) :
    BaseViewModel(baseViewModelFactory.create()) {

    data class CapturedPhotoMeta(
        val file: File,
        val latitude: Double?,
        val longitude: Double?,
        val address: String?
    )

    val showRetakeButtons = MutableLiveData(false)
    val capturedPhotoMeta = MutableLiveData<CapturedPhotoMeta?>(null)

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
}