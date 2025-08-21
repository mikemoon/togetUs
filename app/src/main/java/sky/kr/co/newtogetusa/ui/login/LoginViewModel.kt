package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryMapViewModel.Event
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
                                         private val authRepository: AuthRepository
): BaseViewModel(baseViewModelFactory.create()) {

    fun loginKakao(token:String) = viewModelScope.launch {
        val response = authRepository.loginKakao(hashMapOf(
           "access_token" to token
        ))
        when(response){
            is ResultWrapper.Success -> {

            }
            else -> {

            }
        }
    }

    fun loginNaver(token:String) = viewModelScope.launch {
        val response = authRepository.loginNaver(hashMapOf(
            "access_token" to token
        ))
        when(response){
            is ResultWrapper.Success -> {

            }
            else -> {

            }
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event{
        object KakaoLogin: Event()
        object NaverLogin: Event()
        object GoogleLogin: Event()
        object EmailLogin: Event()
    }
}