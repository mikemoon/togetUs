package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.users.req.ProfileImageRequest
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.my.MyViewModel.Event
import javax.inject.Inject

@HiltViewModel
class ModifyProfileViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val userRepository: UserRepository
) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    var isProfileImageChanged = MutableStateFlow(false)

    fun setNickName(userId: Int, req: HashMap<String, String>, callback: (Boolean) -> Unit) = viewModelScope.launch {
        when (val response = userRepository.putProfileNickname(userId, req)) {
            is ResultWrapper.Success -> {
                callback.invoke(response.data)
            }

            else -> {}
        }
    }

    fun setProfileImage(userId: Int, req: ProfileImageRequest, callback: (Boolean) -> Unit) = viewModelScope.launch {
        when (val response = userRepository.putProfileImage(userId, req)) {
            is ResultWrapper.Success -> {
                callback.invoke(response.data)
            }
            else -> {}
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