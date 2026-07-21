package sky.kr.co.newtogetusa.ui.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginEmailViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                              private val authRepository: AuthRepository,
                                              private val tokenStore: TokenStore
)
    :BaseViewModel(baseViewModelDependenciesFactory.create()) {

        val isEnableLoginBtn = MutableStateFlow(false)
    private val _showEmailClearIcon = MutableLiveData<Boolean>(false)
    val showEmailClearIcon: LiveData<Boolean> = _showEmailClearIcon

    val failMessage = SingleLiveEvent<String>()

    private val _loginResult = MutableLiveData<JoinResponse>()
    val loginResult : LiveData<JoinResponse> = _loginResult
    fun login(email:String, password:String) = viewModelScope.launch {
        val response = authRepository.loginFromEmail(
            hashMapOf(
                "email" to email,
                "pw" to password
            )
        )
        when(response){
            is ResultWrapper.Success -> {
                tokenStore.setTokens(response.data.accessToken, response.data.refreshToken)
                dataStoreRepository.clearString(DataStoreKey.KEY_PROFILE)
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, response.data.refreshToken)
                dataStoreRepository.putInt(DataStoreKey.RECENT_LOGIN_TYPE, LoginViewModel.EMAIL)
                dataStoreRepository.putString(DataStoreKey.KEY_LOGIN_EMAIL, email)
                _loginResult.value = response.data
            }
            is ResultWrapper.GenericError -> {
                Timber.d("login error : ${response}")
                failMessage.value = response.message?.ifBlank { null } ?: "오류가 발생했습니다.\n잠시 후 다시 시도해 주세요."
            }
            is ResultWrapper.NetworkError -> {
                failMessage.value = "오류가 발생했습니다.\n잠시 후 다시 시도해 주세요."
            }
        }
    }

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
        object FindPassword : Event()
    }
}
