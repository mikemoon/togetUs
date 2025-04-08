package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ChattingReportViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory): BaseViewModel(baseViewModelFactory.create()) {

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        data class Reason(val reasonType:Int, val reason:String): Event()
    }
}