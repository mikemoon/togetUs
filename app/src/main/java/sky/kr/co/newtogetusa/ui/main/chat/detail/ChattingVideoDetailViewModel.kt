package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ChattingVideoDetailViewModel @Inject constructor() : BaseViewModel() {

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event{
        object Back : Event()
        object PausePlay : Event()
    }
}