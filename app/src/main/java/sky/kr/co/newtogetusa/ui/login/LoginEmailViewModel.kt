package sky.kr.co.newtogetusa.ui.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class LoginEmailViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()) {

        val isEnableLoginBtn = MutableStateFlow(false)
    private val _showEmailClearIcon = MutableLiveData<Boolean>(false)
    val showEmailClearIcon: LiveData<Boolean> = _showEmailClearIcon

    val onClearEmailClickListener = View.OnClickListener {
        onClearEmailClick()
    }

    fun onClearEmailClick() {
        _email.value = ""
        _showEmailClearIcon.value = false
    }
    private val _email = MutableLiveData<String>("")
    val email: LiveData<String> = _email

    fun onEmailTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _email.value = s.toString()
        _showEmailClearIcon.value = s.isNotEmpty()
        updateLoginBtnEnabled()
    }

    private val _password = MutableLiveData<String>("")
    val password: LiveData<String> = _password
    private val _showPasswordClearIcon = MutableLiveData<Boolean>(false)
    val showPasswordClearIcon: LiveData<Boolean> = _showPasswordClearIcon
    fun onPasswordTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _password.value = s.toString()
        _showPasswordClearIcon.value = s.isNotEmpty()
        updateLoginBtnEnabled()
    }
    val onClearPasswordClickListener = View.OnClickListener {
        onClearPasswordClick()
    }
    fun onClearPasswordClick() {
        _password.value = ""
        _showPasswordClearIcon.value = false
    }

    private fun updateLoginBtnEnabled() {
        isEnableLoginBtn.value = !email.value.isNullOrEmpty() && !password.value.isNullOrEmpty()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object Login : Event()
        object JoinByEmail : Event()

    }
}