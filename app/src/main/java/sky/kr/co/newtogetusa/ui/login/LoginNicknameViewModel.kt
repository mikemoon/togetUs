package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginNicknameViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                 private val authRepository: AuthRepository
)
    :BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val userId = MutableStateFlow(0)
    val verifyCode = MutableStateFlow("")

    private val nicknameRegex = Regex("^[가-힣A-Za-z0-9]{2,10}$")

    fun isNicknameValid(s: String): Boolean = nicknameRegex.matches(s)

    private val _nickname = MutableLiveData<String>("")
    val nickname: LiveData<String> = _nickname
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        _nickname.value = s.toString()
        _showClearIcon.value = s.isNotEmpty()
    }

    private val _checkingNickname = MutableLiveData<Boolean?>(null)
    val checkingNickname: LiveData<Boolean?> = _checkingNickname
    fun checkNickname(nickname:String) = viewModelScope.launch {
        val response = authRepository.checkNickname(nickname)
        when(response){
            is ResultWrapper.Success -> {
                _checkingNickname.value = response.data
            }
            else -> {

            }
        }
    }

    fun join(termsList: List<String>, callback: (Boolean) -> Unit) = viewModelScope.launch {
        val response = authRepository.join(
            hashMapOf(
                "user_id" to userId.value,
                "verify_code" to verifyCode.value,
                "nickname" to nickname.value.toString(),
                "terms_cds" to termsList
            )
        )
        when(response){
            is ResultWrapper.Success -> {
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, response.data.refreshToken)
                callback.invoke(true)
            }
            else -> {
                callback.invoke(false)
            }
        }
    }

    private val _showClearIcon = MutableLiveData<Boolean>(false)
    val showClearIcon: LiveData<Boolean> = _showClearIcon

    fun onClearClick() {
        _nickname.value = ""
        _showClearIcon.value = false
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object Confirm : Event()
    }
}