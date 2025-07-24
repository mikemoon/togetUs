package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){


    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        data class FAQDetail(val id:Int) : Event()
        object Ask : Event()
        object AskHistory : Event()
    }
}