package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.data.remote.ErrorData
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.api.RefreshApi
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
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi
): BaseViewModel(baseViewModelFactory.create()) {

    var recentLoginType = MutableStateFlow(-1)
    var refreshToken = MutableStateFlow("")
    var isFirstRun = MutableStateFlow(true)

    var uiErrorMsg = MutableStateFlow("")

    init {
        viewModelScope.launch {
            dataStoreRepository.getString(DataStoreKey.KEY_TOKEN)?.let {
                tokenStore.setAccessToken(it)
            }
            dataStoreRepository.getString(DataStoreKey.KEY_REFRESH_TOKEN)?.let {
                refreshToken.value = it
                tokenStore.setRefreshToken(it)
            }
            recentLoginType.value = dataStoreRepository.getInt(DataStoreKey.RECENT_LOGIN_TYPE)?:-1

            dataStoreRepository.getBoolean(DataStoreKey.KEY_IS_FIRST_RUN)?.let {
                isFirstRun.value = it
            }
        }
    }

    fun setFirstRun() = viewModelScope.launch {
        dataStoreRepository.putBoolean(DataStoreKey.KEY_IS_FIRST_RUN, false)
    }


    fun refreshToken(refreshToken: String, callback: (tokenTaken: Boolean) -> Unit) =
        viewModelScope.launch {
            if (refreshToken.isBlank()) {
                callback(false); return@launch
            }
            val hasToken = runCatching {
                val resp = withContext(Dispatchers.IO) {
                    refreshApi.reissue(hashMapOf("refreshToken" to refreshToken)).execute()
                }
                if (resp.isSuccessful) {
                    resp.body()?.let { joinResponse ->
                        dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, joinResponse.accessToken)
                        dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, joinResponse.refreshToken)
                        tokenStore.setTokens(joinResponse.accessToken, joinResponse.refreshToken)
                        true
                    } ?: false
                } else {
                    Timber.w("reissue failed: code=${resp.code()} body=${resp.errorBody()?.string()}")
                    uiErrorMsg.emit("인증이 만료되었습니다. 다시 로그인해주세요.")
                    false
                }
            }.getOrElse { e ->
                Timber.e(e, "reissue exception")
                false
            }
            callback(hasToken)
        }

    fun loginKakao(token:String, callback:(resultCode:Int, errorData: ErrorData?)-> Unit) = viewModelScope.launch {
        val response = authRepository.loginKakao(hashMapOf(
           "access_token" to token
        ))
        when(response){
            is ResultWrapper.Success -> {
                tokenStore.setTokens(response.data.accessToken, response.data.refreshToken)
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, response.data.refreshToken)
                dataStoreRepository.putInt(DataStoreKey.RECENT_LOGIN_TYPE, KAKAO)
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
                tokenStore.setTokens(response.data.accessToken, response.data.refreshToken)
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, response.data.refreshToken)
                dataStoreRepository.putInt(DataStoreKey.RECENT_LOGIN_TYPE, NAVER)
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

    fun loginGoogle(token:String, callback:(resultCode:Int, errorData: ErrorData?)-> Unit) = viewModelScope.launch {
        val response = authRepository.loginGoogle(hashMapOf(
            "access_token" to token
        ))
        when(response){
            is ResultWrapper.Success -> {
                tokenStore.setTokens(response.data.accessToken, response.data.refreshToken)
                dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, response.data.accessToken)
                dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, response.data.refreshToken)
                dataStoreRepository.putInt(DataStoreKey.RECENT_LOGIN_TYPE, GOOGLE)
            }
            is ResultWrapper.GenericError -> {
                callback.invoke(response.code?.toInt()?:0, response.errorData)
            }
            else -> {
                Timber.d("google login fail $response")
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

    companion object{
        const val KAKAO = 1
        const val NAVER = 2
        const val GOOGLE = 3
        const val EMAIL = 4
    }
}