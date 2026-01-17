package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartViewModel.Event
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val userRepository: UserRepository) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isModeChanging = MutableStateFlow(false)
    val isPlayerModeFlow = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull().collectLatest {
                isPlayerModeFlow.value = it
            }
        }
    }

    val profileDto = MutableStateFlow<ProfileDto?>(null)
    fun getMyProfile(result: (ProfileDto) -> Unit)= viewModelScope.launch {
        val profileData = dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)
        if(profileData != null){
            result.invoke(profileData)
            profileDto.value = profileData
        }else {
            when (val response = userRepository.getMyProfile()) {
                is ResultWrapper.Success -> {
                    dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                    profileDto.value = response.data
                    result.invoke(response.data)
                }

                else -> {}
            }
        }
    }


    fun onModeChange(isPlayerMode: Boolean) {
        isModeChanging.value = true
        isModeChanging.value = false
        /*viewModelScope.launch {
            delay(3000)
            dataStoreRepository.putBoolean(DataStoreKey.KEY_IS_MODE_PLAYER, isPlayerMode)
            isPlayerModeFlow.value = isPlayerMode
            isModeChanging.value = false
        }*/
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object MySetting : Event()
        object ProfileManage : Event()
        object UseHistory : Event()
        object Settle : Event()
        object FavorPlayer : Event()
        object Favor : Event()
        object Notice : Event()
        object FAQ : Event()
        object Term : Event()
        object JoinPlayer : Event()
        object AccompanyCredit : Event()
    }
}