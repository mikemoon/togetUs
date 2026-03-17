package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class WithDrawViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val myRepository: MyRepository,
    private val authRepository: AuthRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val loginType = MutableStateFlow<Int?>(null)
    val withDrawStep = MutableStateFlow(1)
    val isSnsVerified = MutableStateFlow(false)
    val isEmailVerified = MutableStateFlow(false)


    init {
        viewModelScope.launch {
            loginType.value = dataStoreRepository.getInt(DataStoreKey.RECENT_LOGIN_TYPE)
        }
    }

    fun setSnsVerified(verified: Boolean) {
        isSnsVerified.value = verified
    }

    fun withDraw(callback:(Boolean)->Unit) = viewModelScope.launch {
        when (val res = myRepository.deleteWithdraw()) {
            is ResultWrapper.Success -> {
                callback(res.data)
            }

            else -> {}
        }
    }

    fun verifyEmail(password: String, callback: (Boolean) -> Unit) = viewModelScope.launch {
        val email = ""
        when(val res = authRepository.verifyEmail(hashMapOf("email" to email,"pw" to password))){
            is ResultWrapper.Success ->{
                isEmailVerified.value = res.data
                callback(res.data)
            }
            else ->{}
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object VerifyEmail : Event()
        object WithDraw : Event()
    }
}