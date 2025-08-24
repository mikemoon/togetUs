package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class LoginTermAgreeViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

        val userId = MutableStateFlow(0)
    val verifyCode = MutableStateFlow("")

    val agreeTermPlus = MutableStateFlow(false)
    val agreePrivacy = MutableStateFlow(false)
    val agreeSendPrivacy = MutableStateFlow(false)
    val agreeLocation = MutableStateFlow(false)
    val agreeMarketing = MutableStateFlow(false)

    val isAllAgreeChecked = MutableStateFlow(false)
    val agreeButtonEnable = MutableStateFlow(false)

    fun onAgreeTermPlusChange(isChecked: Boolean) {
        agreeTermPlus.value = isChecked
        updateButtonState()
    }

    fun onAgreePrivacyChange(isChecked: Boolean) {
        agreePrivacy.value = isChecked
        updateButtonState()
    }

    fun onAgreeSendPrivacyChange(isChecked: Boolean) {
        agreeSendPrivacy.value = isChecked
        updateButtonState()
    }

    fun onAgreeLocationChange(isChecked: Boolean) {
        agreeLocation.value = isChecked
        updateButtonState()
    }

    fun onAgreeMarketingChange(isChecked: Boolean) {
        agreeMarketing.value = isChecked
        updateAllAgreeState()
    }

    fun onAgreeAllCheckedChange(isChecked: Boolean) {
        agreeTermPlus.value = isChecked
        agreePrivacy.value = isChecked
        agreeSendPrivacy.value = isChecked
        agreeLocation.value = isChecked
        agreeMarketing.value = isChecked
        updateButtonState()
    }

    private fun updateButtonState() {
        agreeButtonEnable.value = agreeTermPlus.value &&
                agreePrivacy.value &&
                agreeSendPrivacy.value &&
                agreeLocation.value
        updateAllAgreeState()
    }

    private fun updateAllAgreeState() {
        isAllAgreeChecked.value = agreeTermPlus.value &&
                agreePrivacy.value &&
                agreeSendPrivacy.value &&
                agreeLocation.value &&
                agreeMarketing.value
    }


    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object Agree : Event()
    }
}