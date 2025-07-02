package sky.kr.co.newtogetusa.ui.login

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class LoginStartViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        val event = SingleLiveEvent<Event>()
        fun onEventClick(event: Event){
            this.event.value = event
        }

        sealed class Event{
            object Start : Event()
        }
}