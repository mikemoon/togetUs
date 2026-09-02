package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginTermAgreeViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                  private val authRepository: AuthRepository
) :
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

    val termsList = MutableStateFlow<List<TermMeta>?>(null)

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


    fun getTerms() = viewModelScope.launch {
        val response = authRepository.getTerms()
        when(response) {
            is ResultWrapper.Success -> {
                Timber.d("terms : ${response.data}")
                termsList.value = response.data
            }

            else -> {}
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    fun onTermDetailClick(index: Int) {
        val fallback = fallbackTerms.getOrNull(index) ?: return
        val term = termsList.value?.getOrNull(index)
        _event.value = Event.TermDetail(
            title = term?.name?.takeIf { it.isNotBlank() } ?: fallback.title,
            content = term?.description?.takeIf { it.isNotBlank() } ?: fallback.content
        )
    }

    sealed class Event {
        object Back : Event()
        object Agree : Event()
        data class TermDetail(val title: String, val content: String) : Event()
    }

    private data class FallbackTerm(
        val title: String,
        val content: String
    )

    private companion object {
        val fallbackTerms = listOf(
            FallbackTerm("투겟어스 플러스 이용약관", "투겟어스 플러스 이용약관입니다."),
            FallbackTerm("개인정보 처리방침", "투겟어스 개인정보 처리방침입니다."),
            FallbackTerm("개인 정보의 제3자 제공 동의", "개인 정보의 제3자 제공 동의 안내입니다."),
            FallbackTerm("위치정보 이용 동의", "위치정보 수집 및 이용에 대한 안내입니다."),
            FallbackTerm("마케팅 정보 수신동의", "마케팅 정보 수신 동의 안내입니다.")
        )
    }
}
