package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.login.LoginViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileManageSubVM  @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory):
     BaseViewModel(baseViewModelDependenciesFactory.create()) {


    private val _loginEmail = MutableStateFlow("-")
    val loginEmail = _loginEmail.asStateFlow()

    private val _loginTypeLabel = MutableStateFlow("-")
    val loginTypeLabel = _loginTypeLabel.asStateFlow()

    private val _loginAccountText = MutableStateFlow("-")
    val loginAccountText = _loginAccountText.asStateFlow()

    init {
        viewModelScope.launch {
            val email = dataStoreRepository.getString(DataStoreKey.KEY_LOGIN_EMAIL).orEmpty().ifBlank { "-" }
            val loginType = dataStoreRepository.getInt(DataStoreKey.RECENT_LOGIN_TYPE) ?: -1
            val typeLabel = when (loginType) {
                LoginViewModel.KAKAO -> "카카오"
                LoginViewModel.NAVER -> "네이버"
                LoginViewModel.GOOGLE -> "구글"
                LoginViewModel.EMAIL -> "이메일"
                else -> "-"
            }
            _loginEmail.value = email
            _loginTypeLabel.value = typeLabel
            _loginAccountText.value = if (loginType == LoginViewModel.EMAIL) email else typeLabel
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }
    sealed class Event {
        object Back : Event()
        object ModifyProfile : Event()
        object PasswordSet : Event()

        object Recertification : Event()
    }
}
