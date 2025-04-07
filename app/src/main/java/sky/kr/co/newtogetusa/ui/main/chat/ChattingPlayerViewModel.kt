package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem
import javax.inject.Inject

@HiltViewModel
class ChattingPlayerViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory) : BaseViewModel(baseViewModelFactory.create()) {

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> get() = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    fun onChatItemClick(item: ChatListItem) {
        _event.value = Event.ChattingSelect(item)
    }

    sealed class Event {
        data class ChattingSelect(
            val item: ChatListItem
        ) : Event()
    }
}