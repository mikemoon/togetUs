package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.ChangeEmailPasswordResponse
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginPasswordSetViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val authRepository: AuthRepository
)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        val confirmButtonEnable = MutableLiveData(false)

    private val _passwordText = MutableLiveData<String>("")
    val passwordText: LiveData<String> = _passwordText
    fun onPasswordTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _passwordText.value = s.toString()
        checkingComparePassword()
    }

    private val _passwordConfirmText = MutableLiveData<String>("")
    val passwordConfirmText: LiveData<String> = _passwordConfirmText
    fun onPasswordConfirmTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _passwordConfirmText.value = s.toString()
        checkingComparePassword()
    }

    fun checkingComparePassword() {
        if (_passwordText.value == _passwordConfirmText.value) {
            confirmButtonEnable.value = true
        } else {
            confirmButtonEnable.value = false
        }
    }

    private val _passwordChangeResult = MutableLiveData<ChangeEmailPasswordResponse?>()
    val passwordChangeResult: LiveData<ChangeEmailPasswordResponse?> = _passwordChangeResult
    fun changePassword(email: String, password: String, verifyCode:String) = viewModelScope.launch {
        val response =
            authRepository.changeEmailPassword(
                hashMapOf(
                    "email" to email,
                    "pw" to password,
                    "verify_code" to verifyCode
                )
            )
        Timber.d("changePw $response")
        when(response){
            is ResultWrapper.Success ->{
                Timber.d("changePw success $response")
                _passwordChangeResult.value = response.data
            }
            else ->{

            }
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object InputComplete : Event()
    }
}
