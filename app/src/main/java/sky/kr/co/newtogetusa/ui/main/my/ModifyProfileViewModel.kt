package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.data.remote.request.user.ProfileImageRequest
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.my.MyViewModel.Event
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ModifyProfileViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val userRepository: UserRepository,
    private val playerRepository: PlayerRepository
) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    var isProfileImageChanged = MutableStateFlow(false)

    val errorMsg = MutableStateFlow("")

    fun setNickName(userId: Int, req: HashMap<String, String>, callback: (Boolean) -> Unit) = viewModelScope.launch {
        when (val response = userRepository.putProfileNickname(userId, req)) {
            is ResultWrapper.Success -> {
                callback.invoke(response.data)
            }

            is ResultWrapper.GenericError ->{
                errorMsg.value = response.message.toString()
            }
            else -> {
                Timber.e("error $response")
            }
        }
    }

    fun setProfileImage(userId: Int, req: ProfileImageRequest, callback: (Boolean) -> Unit) = viewModelScope.launch {
        when (val response = userRepository.putProfileImage(userId, req)) {
            is ResultWrapper.Success -> {
                callback.invoke(response.data)
            }
            is ResultWrapper.GenericError -> {
                errorMsg.value = response.message.toString()
                Timber.e("setProfileImage error ${response.code}: ${response.message}")
            }
            is ResultWrapper.NetworkError -> {
                errorMsg.value = "네트워크 연결을 확인해주세요."
                Timber.e("setProfileImage network error")
            }
        }
    }

    fun setPlayerProfileImage(playerId: Int, req: PlayerProfileImageRequest, callback: (Boolean) -> Unit) = viewModelScope.launch {
        when (val response = playerRepository.postProfileImage(playerId, req)) {
            is ResultWrapper.Success -> {
                callback.invoke(response.data)
            }
            is ResultWrapper.GenericError -> {
                errorMsg.value = response.message.toString()
                Timber.e("setPlayerProfileImage error ${response.code}: ${response.message}")
            }
            is ResultWrapper.NetworkError -> {
                errorMsg.value = "네트워크 연결을 확인해주세요."
                Timber.e("setPlayerProfileImage network error")
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
        object Save : Event()
        object ProfileImage : Event()
    }
}
