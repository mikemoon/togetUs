package sky.kr.co.newtogetusa.ui.login

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginJoinEmailViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val authRepository: AuthRepository)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        val requestVerifyCodeButtonEnable = MutableLiveData<Boolean>(false)
        val resendButtonEnable = MutableLiveData<Boolean>(true)
        private val resendRemainingCount = MutableLiveData(4)

    val titleText = MutableStateFlow("로그인에 사용할 이메일을 입력해 주세요")
    val isPasswordFindModeStateFlow = MutableStateFlow(false)

    fun setIsPasswordMode(isPasswordFindMode: Boolean) {
        isPasswordFindModeStateFlow.value = isPasswordFindMode
        if (isPasswordFindMode) {
            titleText.value = "이메일을 입력해 주세요"
        } else {
            titleText.value = "로그인에 사용할 이메일을 입력해 주세요"
        }
    }

    val verifyLayoutMode = MutableStateFlow(false)

    // 타이머 관련
    private var countDownTimer: CountDownTimer? = null
    private val _timeoutSeconds = MutableLiveData<Int>(0)
    val timeoutSeconds: LiveData<Int> = _timeoutSeconds

    private val _timeoutText = MutableLiveData<String>("")
    val timeoutText: LiveData<String> = _timeoutText

    private val _isTimeoutActive = MutableLiveData<Boolean>(false)
    val isTimeoutActive: LiveData<Boolean> = _isTimeoutActive

    private val _isTimeoutExpired = MutableLiveData<Boolean>(false)
    val isTimeoutExpired: LiveData<Boolean> = _isTimeoutExpired

    private val _emailText = MutableLiveData<String>("")
    val emailText: LiveData<String> = _emailText
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        _emailText.value = s.toString()
        _showClearIcon.value = s.isNotEmpty()
        requestVerifyCodeButtonEnable.value = isValidEmailFormat(s.toString())
    }

    private val _verifyEmailResult = MutableLiveData<Boolean>()
    val verifyEmailResult : LiveData<Boolean> = _verifyEmailResult
    fun requestVerifyEmail(email: String, isResend: Boolean = false) = viewModelScope.launch {
        if (loadingState.value) return@launch
        loadingState.value = true
        val response:ResultWrapper<Boolean> = authRepository.verifyEmail(
            hashMapOf(
                "email" to email,
                "os" to "A"
            )
        )
        loadingState.value = false
        when(response){
            is ResultWrapper.Success -> {
                val success = response.data == true
                _verifyEmailResult.value = success
                if (success) {
                    if (isResend) {
                        val remaining = ((resendRemainingCount.value ?: 0) - 1).coerceAtLeast(0)
                        resendRemainingCount.value = remaining
                        resendButtonEnable.value = remaining > 0
                    }
                    _event.value = Event.VerifyEmailRequested
                } else {
                    _event.value = Event.ShowMessage("인증번호 요청에 실패했습니다.")
                }
            }
            is ResultWrapper.GenericError -> {
                _event.value = Event.ShowMessage(response.message ?: "인증번호 요청에 실패했습니다.")
            }
            ResultWrapper.NetworkError -> {
                _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
        }
    }

    private val _certCodeResult = MutableLiveData<Boolean>()
    val certCodeResult : LiveData<Boolean> = _certCodeResult
    fun certifyVerifyCode(code:String) = viewModelScope.launch {
        if (loadingState.value) return@launch
        loadingState.value = true
        val response:ResultWrapper<Boolean> = authRepository.cerifyVerifyCode(
            hashMapOf(
                "email" to emailText.value.toString(),
                "verify_code" to code,
                "os" to "A"
            )
        )
        loadingState.value = false
        when(response){
            is ResultWrapper.Success -> {
                _certCodeResult.value = response.data == true
            }
            else ->{

            }
        }
    }

    private val _showClearIcon = MutableLiveData<Boolean>(false)
    val showClearIcon: LiveData<Boolean> = _showClearIcon

    fun onClearClick() {
        _emailText.value = ""
        _showClearIcon.value = false
    }

    private val _verifyCodeText = MutableLiveData<String>("")
    val verifyCodeText: LiveData<String> = _verifyCodeText
    fun onVerifyCodeTextChanged(s: CharSequence, start: Int, before: Int, count: Int){
        _verifyCodeText.value = s.toString()
    }

    private fun isValidEmailFormat(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun startTimeout(durationMinutes: Int = 10) {
        val totalSeconds = durationMinutes * 60
        _isTimeoutActive.value = true
        _isTimeoutExpired.value = false

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                _timeoutSeconds.value = secondsLeft
                _timeoutText.value = formatTime(secondsLeft)
            }

            override fun onFinish() {
                _timeoutSeconds.value = 0
                _timeoutText.value = "00:00"
                _isTimeoutActive.value = false
                _isTimeoutExpired.value = true
            }
        }
        countDownTimer?.start()
    }

    fun resendRemainingMessage(): String =
        "요청 가능 횟수가 ${resendRemainingCount.value ?: 0}회 남았어요."

    // 타이머 중지
    fun stopTimeout() {
        countDownTimer?.cancel()
        _isTimeoutActive.value = false
        _isTimeoutExpired.value = false
        _timeoutSeconds.value = 0
        _timeoutText.value = ""
    }

    // 시간 포맷팅 (MM:SS)
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }


    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    sealed class Event {
        object Back : Event()
        object RequestVerifyCode : Event()
        object ReSend : Event()
        object VerifyEmailRequested : Event()
        data class ShowMessage(val message: String) : Event()
    }
}
