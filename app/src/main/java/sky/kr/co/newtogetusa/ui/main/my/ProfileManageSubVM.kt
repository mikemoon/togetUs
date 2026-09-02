package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.player.PortOneConfigDto
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.login.LoginViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileManageSubVM  @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository
):
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
                LoginViewModel.APPLE -> "애플"
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
        when (event) {
            Event.Recertification -> startIdentityVerification()
            else -> _event.value = event
        }
    }

    private fun startIdentityVerification() = viewModelScope.launch {
        when (val res = playerRepository.getPortOneConfig()) {
            is ResultWrapper.Success -> {
                _event.value = Event.StartIdentityVerification(res.data)
            }
            is ResultWrapper.GenericError -> {
                _event.value = Event.ShowMessage(res.message ?: "본인인증 설정을 불러오지 못했습니다.")
            }
            is ResultWrapper.NetworkError -> {
                _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
        }
    }

    fun verifyIdentity(identityVerificationId: String) = viewModelScope.launch {
        when (val res = playerRepository.verifyIdentity(identityVerificationId)) {
            is ResultWrapper.Success -> {
                _event.value = Event.IdentityVerified
            }
            is ResultWrapper.GenericError -> {
                _event.value = Event.ShowMessage(res.message ?: "본인인증 검증에 실패했습니다.")
            }
            is ResultWrapper.NetworkError -> {
                _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
        }
    }

    sealed class Event {
        object Back : Event()
        object ModifyProfile : Event()
        object PasswordSet : Event()

        object Recertification : Event()
        data class StartIdentityVerification(val config: PortOneConfigDto) : Event()
        object IdentityVerified : Event()
        data class ShowMessage(val message: String) : Event()
    }
}
