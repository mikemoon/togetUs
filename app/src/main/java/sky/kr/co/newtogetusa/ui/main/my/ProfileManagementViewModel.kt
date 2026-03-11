package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ProfileManagementViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                     private val userRepository: UserRepository,
    private val deliveryRepository: DeliveryRepository
)
    : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val profileDto = MutableStateFlow<ProfileDto?>(null)
    fun getMyProfile(result: (ProfileDto) -> Unit)= viewModelScope.launch {
        when(val response = userRepository.getMyProfile()){
            is ResultWrapper.Success ->{
                dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                profileDto.value = response.data
                result.invoke(response.data)
            }
            else ->{}
        }
    }

    fun getDeliveryList()= viewModelScope.launch {
        when(val res = deliveryRepository.postDeliverySearch(DeliverySearchReq(
            type = "ALL",
            title = "",
            page_no = 0,
        )
        )
        ){
            is ResultWrapper.Success ->{

            }
            else ->{}
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
    }
}