package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ErrorData
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryMapViewModel.Event
import timber.log.Timber
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


    fun loginNaver(token:String, callback:(resultCode:Int, errorData: ErrorData?)-> Unit) = viewModelScope.launch {
        val response = authRepository.loginNaver(hashMapOf(
            "access_token" to token
        ))
        when(response){
            is ResultWrapper.Success -> {
                Timber.d("naver login success $response")
                callback.invoke(200, null)
            }
            is ResultWrapper.GenericError -> {
                Timber.d("naver login fail ${response}")
                callback.invoke(response.code?.toInt()?:0, response.errorData)
            }
            else -> {
                Timber.d("naver login fail $response")
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