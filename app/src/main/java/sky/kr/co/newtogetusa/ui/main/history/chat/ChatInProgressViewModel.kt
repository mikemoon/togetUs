package sky.kr.co.newtogetusa.ui.main.history.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ChatInProgressDto
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ChatInProgressViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val rooms = MutableStateFlow<List<ChatInProgressDto>>(emptyList())

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun load(deliveryId: Long) = viewModelScope.launch {
        if (deliveryId <= 0L) return@launch
        loadingState.value = true
        when (val res = deliveryRepository.getChatInProgressList(deliveryId)) {
            is ResultWrapper.Success -> rooms.value = res.data
            else -> _event.value = Event.LoadFailed
        }
        loadingState.value = false
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    fun onRoomClick(roomId: Long) {
        _event.value = Event.OpenRoom(roomId)
    }

    sealed class Event {
        object Back : Event()
        object LoadFailed : Event()
        data class OpenRoom(val roomId: Long) : Event()
    }
}
