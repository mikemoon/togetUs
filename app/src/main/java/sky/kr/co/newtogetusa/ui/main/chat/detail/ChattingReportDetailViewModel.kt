package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.ChatRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ChattingReportDetailViewModel @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val chatRepository: ChatRepository,
) : BaseViewModel(baseViewModelFactory.create()) {


    val titleFlow = MutableStateFlow("")
    val inputTextCountFlow = MutableStateFlow("0/300")

    val editTextChangedFlow = MutableStateFlow("")
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        editTextChangedFlow.value = s.toString()
        inputTextCountFlow.value = "${s.length}/300"
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    fun report(roomId: Long) = viewModelScope.launch {
        if (roomId <= 0L) {
            _event.value = Event.ReportFailed
            return@launch
        }
        when (chatRepository.chatRoomReport(roomId)) {
            is ResultWrapper.Success -> _event.value = Event.ReportSuccess
            else -> _event.value = Event.ReportFailed
        }
    }

    sealed class Event{
        object Back : Event()
        object Send : Event()
        object ReportSuccess : Event()
        object ReportFailed : Event()
    }
}
