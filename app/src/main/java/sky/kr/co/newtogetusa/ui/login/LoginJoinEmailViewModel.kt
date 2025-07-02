package sky.kr.co.newtogetusa.ui.login

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginJoinEmailViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        val requestVerifyCodeButtonEnable = MutableLiveData<Boolean>(false)

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
    }
}