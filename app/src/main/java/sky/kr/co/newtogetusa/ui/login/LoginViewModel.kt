package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryMapViewModel.Event
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory): BaseViewModel(baseViewModelFactory.create()) {

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