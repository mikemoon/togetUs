package sky.kr.co.newtogetusa.ui.main.history.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ChatInProgressDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ChatInProgressLastMessageDto
import sky.kr.co.newtogetusa.repository.ChatRepository
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ChatInProgressViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository,
    private val chatRepository: ChatRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val rooms = MutableStateFlow<List<ChatInProgressDto>>(emptyList())

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun load(deliveryId: Long) = viewModelScope.launch {
        if (deliveryId <= 0L) return@launch
        loadingState.value = true
        when (val res = deliveryRepository.getChatInProgressList(deliveryId)) {
            is ResultWrapper.Success -> {
                rooms.value = res.data
                mergeRoomDetails(res.data)
            }
            else -> _event.value = Event.LoadFailed
        }
        loadingState.value = false
    }

    private suspend fun mergeRoomDetails(baseRooms: List<ChatInProgressDto>) {
        val merged = baseRooms.map { room ->
            viewModelScope.async {
                when (val detail = chatRepository.getChatRoom(room.roomId)) {
                    is ResultWrapper.Success -> {
                        val last = detail.data.last_msg
                        room.copy(
                            lastMsg = last?.let {
                                ChatInProgressLastMessageDto(
                                    roomId = room.roomId,
                                    unreadCnt = detail.data.unread_cnt,
                                    msgId = it.msg_id.toString(),
                                    mimetype = it.mimetype,
                                    msg = it.msg,
                                    sendDate = it.send_date
                                )
                            } ?: room.lastMsg,
                            prdPicture = detail.data.delivery?.prd_picture ?: room.prdPicture
                        )
                    }
                    else -> room
                }
            }
        }.awaitAll()
        rooms.value = merged
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    fun onRoomClick(roomId: Long) {
        _event.value = Event.OpenRoom(roomId)
    }

    fun selectPlayer(deliveryId: Long, playerId: Long) = viewModelScope.launch {
        if (deliveryId <= 0L || playerId <= 0L) return@launch
        _event.value = Event.OpenPayment(deliveryId, playerId)
    }

    sealed class Event {
        object Back : Event()
        object LoadFailed : Event()
        object SelectFailed : Event()
        data class OpenRoom(val roomId: Long) : Event()
        data class OpenPayment(val deliveryId: Long, val playerId: Long) : Event()
    }
}
