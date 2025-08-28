package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.data.remote.ErrorData
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryMapViewModel.Event
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
                                         private val authRepository: AuthRepository,
    private val tokenStore: TokenStore
): BaseViewModel(baseViewModelFactory.create()) {

    init {
        viewModelScope.launch {
            dataStoreRepository.getString(DataStoreKey.KEY_TOKEN)?.let {
                tokenStore.set(it)
            }
        }
    }

    fun loginKakao(token:String, callback:(resultCode:Int, errorData: ErrorData?)-> Unit) = viewModelScope.launch {
        val response = authRepository.loginKakao(hashMapOf(
           "access_token" to token
        ))
        when(response){
            is ResultWrapper.Success -> {
                tokenStore.set(response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
                callback.invoke(200, null)
            }
            is ResultWrapper.GenericError -> {
                Timber.d("naver login fail ${response}")
                callback.invoke(response.code?.toInt()?:0, response.errorData)
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
                tokenStore.set(response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
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