package sky.kr.co.newtogetusa.ui.main.global

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class NoPictureVM @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory) :
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

    companion object {
        const val MAX_REASON_LENGTH = 30
    }
}