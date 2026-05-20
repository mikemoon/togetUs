package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.MqttChatCategory
import sky.kr.co.newtogetusa.chat.MqttMessagePayload
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatMessageDto
import sky.kr.co.newtogetusa.repository.ChatRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.utils.TextConvertUtil.toWon
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChattingConversationViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val callbackManager: MessageCallbackManager,
    private val chatRepository: ChatRepository,
    private val deliveryRepository: DeliveryRepository,
    baseViewModelFactory: BaseViewModelDependenciesFactory
) : BaseViewModel(baseViewModelFactory.create()), MessageHandler {

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val messagesList = mutableListOf<ChatMessage>()
    private val gson = Gson()
    private var currentRoomId: Long = -1
    private var myUserId: Long = 0

    val sendMessageEnable = MutableStateFlow(false)
    val roomNameFlow = MutableStateFlow("")
    val deliveryTitleFlow = MutableStateFlow("")
    val deliveryStatusFlow = MutableStateFlow("")
    val deliveryFeeFlow = MutableStateFlow("")
    val deliveryImageFlow = MutableStateFlow<String?>(null)

    init {
        callbackManager.registerCallback(this)
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun connect() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.CONNECTING

            withContext(Dispatchers.IO) {
                try {
                    chatClient.connect()
                    withContext(Dispatchers.Main) {
                        _connectionState.value = ConnectionState.CONNECTED
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _connectionState.value = ConnectionState.ERROR
                    }
                }
            }
        }
    }

    fun loadRoomMessages(roomId: Long, sinceId: Long = 1, count: Int = 100) {
        if (roomId <= 0) return
        currentRoomId = roomId

        viewModelScope.launch {
            loadRoomHeader(roomId)
            myUserId = dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)?.user?.user_id?.toLong() ?: 0
            when (val response = chatRepository.getRoomMessages(roomId, sinceId, count)) {
                is ResultWrapper.Success -> {
                    messagesList.clear()
                    messagesList.addAll(response.data.msgs.map { it.toChatMessage() })
                    _messages.value = messagesList.toList()

                    response.data.msgs.maxOfOrNull { it.msg_id }?.let { lastMessageId ->
                        chatClient.sendRead(roomId, lastMessageId)
                    }
                }
                is ResultWrapper.GenericError -> {
                    Timber.e("getRoomMessages error ${response.code}: ${response.message}")
                }
                is ResultWrapper.NetworkError -> {
                    Timber.e("getRoomMessages network error")
                }
            }
        }
    }

    private suspend fun loadRoomHeader(roomId: Long) {
        when (val room = chatRepository.getChatRoom(roomId)) {
            is ResultWrapper.Success -> {
                roomNameFlow.value = room.data.room_name
                deliveryStatusFlow.value = room.data.delivery.status_cd
                val deliveryId = room.data.delivery.delivery_id.toLong()
                if (deliveryId > 0L) loadDeliveryHeader(deliveryId)
            }
            else -> {}
        }
    }

    private suspend fun loadDeliveryHeader(deliveryId: Long) {
        when (val delivery = deliveryRepository.getDeliveryDetail(deliveryId)) {
            is ResultWrapper.Success -> {
                deliveryTitleFlow.value = delivery.data.title
                deliveryStatusFlow.value = delivery.data.status_cd
                deliveryFeeFlow.value = delivery.data.fee.fee_final.toWon()
                deliveryImageFlow.value = delivery.data.product.pictures.firstOrNull()
            }
            else -> {}
        }
    }

    fun setChatRoomNotificationOff() = runChatRoomSetting(
        action = { chatRepository.chatRoomNotiOff(currentRoomId) },
        success = Event.ChatActionSuccess("채팅방의 알림이 꺼졌습니다."),
    )

    fun blockChatRoom() = runChatRoomSetting(
        action = { chatRepository.chatRoomBlock(currentRoomId) },
        success = Event.ChatActionSuccess("채팅방이 차단되었습니다."),
    )

    fun exitChatRoom() = runChatRoomSetting(
        action = { chatRepository.chatRoomExit(currentRoomId) },
        success = Event.ChatRoomExited,
    )

    private fun runChatRoomSetting(
        action: suspend () -> ResultWrapper<Boolean>,
        success: Event,
    ) {
        if (currentRoomId <= 0) {
            _event.value = Event.ChatActionFailed("채팅방 정보를 확인할 수 없습니다.")
            return
        }
        viewModelScope.launch {
            when (action()) {
                is ResultWrapper.Success -> _event.value = success
                else -> _event.value = Event.ChatActionFailed("요청 처리에 실패했습니다.")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatClient.disconnect()
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || currentRoomId <= 0) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatClient.sendMessage(currentRoomId, content)
            }
            addMessage(
                ChatMessage(
                    id = System.currentTimeMillis(),
                    sender = "me",
                    content = content,
                    messageType = MESSAGE_TYPE_TEXT,
                    isMyMessage = true
                )
            )
        }
    }

    fun publishRead(payload: String) {
        publish(MqttChatCategory.READ, payload)
    }

    fun publishAttach(payload: String) {
        publish(MqttChatCategory.ATTACH, payload)
    }

    fun publishGps(payload: String) {
        publish(MqttChatCategory.GPS, payload)
    }

    private fun publish(category: MqttChatCategory, payload: String) {
        if (payload.isBlank()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatClient.publish(category, payload)
            }
        }
    }

    fun addMessage(message: ChatMessage) {
        viewModelScope.launch {
            if (messagesList.any { it.id == message.id }) return@launch
            messagesList.add(message)
            _messages.value = messagesList.sortedBy { it.timestamp }
        }
    }

    override fun onCleared() {
        super.onCleared()
        callbackManager.unregisterCallback(this)
    }

    override fun handleIncomingMessage(sender: String, content: String) {
        runCatching {
            val payload = gson.fromJson(content, MqttMessagePayload::class.java)
            if (payload.roomId != currentRoomId) return
            addMessage(payload.toChatMessage(sender))
        }.onFailure {
            addMessage(
                ChatMessage(
                    id = System.currentTimeMillis(),
                    sender = sender,
                    content = content,
                    messageType = sender.toFallbackMessageType(),
                    isMyMessage = false
                )
            )
        }
    }

    override fun onConnectionLost(cause: Throwable?) {
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
        // Delivery acknowledgement is reflected by optimistic UI append and MQTT echo.
    }

    fun onChattingMessage(s: CharSequence, start: Int, before: Int, count: Int) {
        sendMessageEnable.value = s.isNotEmpty()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> get() = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    sealed class Event {
        object Back : Event()
        object PhoneCall : Event()
        object More : Event()
        data class MessageImageSelect(val url: String) : Event()
        data class MessageVideoSelect(val url: String) : Event()
        data class MessageResend(val id: Long) : Event()
        data class MessageDelete(val id: Long) : Event()
        object InputMore : Event()
        object InputSend : Event()
        object InputCamera : Event()
        object InputAlbum : Event()
        object InputMovie : Event()
        data class ChatActionSuccess(val message: String) : Event()
        data class ChatActionFailed(val message: String) : Event()
        object ChatRoomExited : Event()
    }

    private fun ChatMessageDto.toChatMessage(): ChatMessage {
        val messageType = mimetype.toMessageType()
        return ChatMessage(
            id = msg_id,
            sender = send_uname.orEmpty().ifBlank { if (send_uid == 0L) "시스템" else send_uid.toString() },
            content = msg,
            messageType = messageType,
            messageImageUrl = if (messageType == MESSAGE_TYPE_IMAGE) msg else null,
            messageVieoUrl = if (messageType == MESSAGE_TYPE_VIDEO) msg else null,
            timestamp = send_date.toTimestamp(),
            isMyMessage = send_uid != 0L && send_uid == myUserId
        )
    }

    private fun MqttMessagePayload.toChatMessage(category: String): ChatMessage {
        val messageType = mimeType.toMessageType(category)
        val resolvedMessageId = if (messageId > 0) messageId else System.currentTimeMillis()
        return ChatMessage(
            id = resolvedMessageId,
            sender = senderUserName.orEmpty().ifBlank {
                if (senderUserId == 0L) "시스템" else senderUserId.toString()
            },
            content = message,
            messageType = messageType,
            messageImageUrl = if (messageType == MESSAGE_TYPE_IMAGE) message else null,
            messageVieoUrl = if (messageType == MESSAGE_TYPE_VIDEO) message else null,
            timestamp = sendDate?.toTimestamp() ?: System.currentTimeMillis(),
            isMyMessage = senderUserId != 0L && senderUserId == myUserId
        )
    }

    private fun String.toFallbackMessageType(): Int {
        return when (this) {
            MqttChatCategory.ATTACH.topicName -> MESSAGE_TYPE_IMAGE
            else -> MESSAGE_TYPE_TEXT
        }
    }

    private fun String.toMessageType(category: String? = null): Int {
        return when {
            category == MqttChatCategory.ATTACH.topicName && startsWith("image/") -> MESSAGE_TYPE_IMAGE
            category == MqttChatCategory.ATTACH.topicName && startsWith("video/") -> MESSAGE_TYPE_VIDEO
            startsWith("image/") -> MESSAGE_TYPE_IMAGE
            startsWith("video/") -> MESSAGE_TYPE_VIDEO
            else -> MESSAGE_TYPE_TEXT
        }
    }

    private fun String.toTimestamp(): Long {
        val patterns = listOf(
            "yyyy-MM-dd HH:mm:ss.S",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        patterns.forEach { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).parse(this)?.time
            }.getOrNull()?.let { return it }
        }
        return System.currentTimeMillis()
    }

    companion object {
        private const val MESSAGE_TYPE_TEXT = 0
        private const val MESSAGE_TYPE_IMAGE = 1
        private const val MESSAGE_TYPE_VIDEO = 2
    }
}
