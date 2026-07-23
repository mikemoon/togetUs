package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import android.util.Base64
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.MqttChatCategory
import sky.kr.co.newtogetusa.chat.MqttMessagePayload
import sky.kr.co.newtogetusa.chat.MqttReadPayload
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatAttachUploadResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatMessageDto
import sky.kr.co.newtogetusa.repository.ChatRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max

@HiltViewModel
class ChattingConversationViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val callbackManager: MessageCallbackManager,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
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
    private var partnerReadMessageId: Long = 0
    private var hasShownUnavailableNotice = false

    val sendMessageEnable = MutableStateFlow(false)
    val roomNameFlow = MutableStateFlow("")
    val deliveryTitleFlow = MutableStateFlow("")
    val deliveryStatusFlow = MutableStateFlow("")
    val deliveryFeeFlow = MutableStateFlow("")
    val deliveryFeeVisibleFlow = MutableStateFlow(false)
    val deliveryImageFlow = MutableStateFlow<String?>(null)
    val deliveryIdFlow = MutableStateFlow(0L)
    val isBlockedFlow = MutableStateFlow(false)
    val isChatUnavailableFlow = MutableStateFlow(false)
    val isNotificationOnFlow = MutableStateFlow(true)
    val isReportedFlow = MutableStateFlow(false)
    val chatInputEnabled = MutableLiveData(true)

    init {
        callbackManager.registerCallback(this)
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun connect() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            configureChatSession()

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
        if (currentRoomId != roomId) hasShownUnavailableNotice = false
        currentRoomId = roomId

        viewModelScope.launch {
            loadRoomHeader(roomId)
            configureChatSession()
            if (!chatClient.isConnected()) {
                withContext(Dispatchers.IO) {
                    chatClient.connect()
                }
            }
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

    private suspend fun configureChatSession() {
        var profile = dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)
        if (profile == null) {
            when (val response = userRepository.getMyProfile()) {
                is ResultWrapper.Success -> {
                    profile = response.data
                    dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                }
                else -> Unit
            }
        }

        myUserId = profile?.user?.user_id?.toLong() ?: 0
        if (myUserId > 0) {
            chatClient.setSession(
                userNumber = myUserId,
                accessToken = dataStoreRepository.getString(DataStoreKey.KEY_TOKEN)
            )
        }
    }

    private suspend fun loadRoomHeader(roomId: Long) {
        when (val room = chatRepository.getChatRoom(roomId)) {
            is ResultWrapper.Success -> {
                roomNameFlow.value = room.data.room_name
                isBlockedFlow.value = room.data.is_blocked
                isChatUnavailableFlow.value = room.data.is_chat_disabled
                isNotificationOnFlow.value = room.data.isNotificationOn
                isReportedFlow.value = room.data.is_reported
                chatInputEnabled.value = !room.data.is_chat_disabled
                if (room.data.is_chat_disabled && !hasShownUnavailableNotice) {
                    hasShownUnavailableNotice = true
                    _event.value = Event.ChatUnavailable(room.data.chat_disabled_message)
                }
                // 내 read_msg_id가 아니라 상대방의 읽음 위치만 송신 메시지의 읽음 여부에 사용한다.
                partnerReadMessageId = room.data.partner_read_msg_id.toLong()
                deliveryTitleFlow.value = room.data.room_name
                deliveryFeeFlow.value = ""
                deliveryFeeVisibleFlow.value = false
                val roomDelivery = room.data.delivery ?: run {
                    deliveryIdFlow.value = 0L
                    deliveryStatusFlow.value = ""
                    deliveryImageFlow.value = null
                    return
                }
                deliveryIdFlow.value = roomDelivery.delivery_id.toLong()
                deliveryStatusFlow.value = roomDelivery.status_cd
                deliveryImageFlow.value = roomDelivery.prd_picture
            }
            else -> {}
        }
    }

    fun setChatRoomNotificationOn() = runChatRoomSetting(
        action = { chatRepository.chatRoomNotiOn(currentRoomId) },
        success = Event.ChatRoomNotificationOn,
    )

    fun setChatRoomNotificationOff() = runChatRoomSetting(
        action = { chatRepository.chatRoomNotiOff(currentRoomId) },
        success = Event.ChatRoomNotificationOff,
    )

    fun blockChatRoom() = runChatRoomSetting(
        action = { chatRepository.chatRoomBlock(currentRoomId) },
        success = Event.ChatRoomBlocked,
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
        if (content.isBlank() || currentRoomId <= 0 || isChatUnavailableFlow.value) return

        viewModelScope.launch {
            val messagePointerId = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                chatClient.sendMessage(
                    roomId = currentRoomId,
                    message = content,
                    messagePointerId = 0
                )
            }
            addMessage(
                ChatMessage(
                    id = -messagePointerId,
                    messagePointerId = messagePointerId,
                    sender = "me",
                    content = content,
                    messageType = MESSAGE_TYPE_TEXT,
                    isMyMessage = true,
                    isUnread = true
                )
            )
        }
    }

    fun sendImage(base64: String, mimeType: String, onComplete: (Boolean) -> Unit = {}) {
        if (base64.isBlank() || currentRoomId <= 0 || isChatUnavailableFlow.value) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val localMessageId = System.currentTimeMillis()
            val attach = uploadAttach(base64, mimeType, localMessageId)
            if (attach == null || attach.url.isBlank()) {
                _event.value = Event.ChatActionFailed("이미지 전송에 실패했습니다.")
                onComplete(false)
                return@launch
            }
            addMessage(
                ChatMessage(
                    id = attach.msg_id.takeIf { it > 0 } ?: -localMessageId,
                    messagePointerId = attach.msg_ptr_id,
                    sender = attach.send_uname.ifBlank { "me" },
                    content = attach.url,
                    messageType = MESSAGE_TYPE_IMAGE,
                    messageImageUrl = attach.url,
                    timestamp = attach.send_date.toTimestamp(),
                    isMyMessage = true,
                    isUnread = true
                )
            )
            onComplete(true)
        }
    }

    private suspend fun uploadAttach(base64: String, mimeType: String, localMessageId: Long): ChatAttachUploadResponseDto? {
        val bytes = runCatching { Base64.decode(base64, Base64.NO_WRAP) }.getOrNull() ?: return null
        val extension = when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.startsWith("video/", ignoreCase = true) -> "mp4"
            else -> "jpg"
        }
        val body = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "chat_${localMessageId}.$extension",
            body = body
        )
        return when (val response = chatRepository.uploadChatAttach(currentRoomId, 0, part)) {
            is ResultWrapper.Success -> response.data
            is ResultWrapper.GenericError -> {
                Timber.e("uploadChatAttach error ${response.code}: ${response.message}")
                null
            }
            is ResultWrapper.NetworkError -> {
                Timber.e("uploadChatAttach network error")
                null
            }
        }
    }

    fun resendMessage(id: Long) {
        val message = messagesList.firstOrNull { it.id == id } ?: return
        removeMessage(id)
        sendMessage(message.content)
    }

    fun removeMessage(id: Long) {
        val removed = messagesList.removeAll { it.id == id }
        if (removed) {
            _messages.value = messagesList.sortedBy { it.timestamp }
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
            val pendingIndex = messagesList.indexOfFirst {
                it.messagePointerId > 0 && it.messagePointerId == message.messagePointerId
            }
            if (pendingIndex >= 0) {
                messagesList[pendingIndex] = message
                _messages.value = messagesList.sortedBy { it.timestamp }
                return@launch
            }
            val fallbackPendingIndex = messagesList.indexOfFirst {
                message.isMyMessage &&
                    it.id < 0 &&
                    it.isMyMessage &&
                    (
                        it.content == message.content ||
                            (it.messageType == MESSAGE_TYPE_IMAGE && message.messageType == MESSAGE_TYPE_IMAGE)
                        ) &&
                    abs(it.timestamp - message.timestamp) < PENDING_MESSAGE_MATCH_WINDOW_MS
            }
            if (fallbackPendingIndex >= 0) {
                messagesList[fallbackPendingIndex] = message
                _messages.value = messagesList.sortedBy { it.timestamp }
                return@launch
            }
            messagesList.add(message)
            _messages.value = messagesList.sortedBy { it.timestamp }
        }
    }

    override fun onCleared() {
        super.onCleared()
        callbackManager.unregisterCallback(this)
    }

    override fun handleIncomingMessage(sender: String, content: String) {
        when (sender) {
            MqttChatCategory.READ.topicName -> {
                handleReadMessage(content)
                return
            }
            MqttChatCategory.MSG.topicName, MqttChatCategory.ATTACH.topicName -> Unit
            else -> return
        }

        runCatching {
            val payload = gson.fromJson(content, MqttMessagePayload::class.java)
            if (payload.roomId != currentRoomId) return
            addMessage(payload.toChatMessage(sender))
            publishReadForIncomingMessage(payload)
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

    private fun publishReadForIncomingMessage(payload: MqttMessagePayload) {
        if (payload.messageId <= 0) return
        if (payload.senderUserId == 0L || payload.senderUserId == myUserId) return

        viewModelScope.launch(Dispatchers.IO) {
            chatClient.sendRead(payload.roomId, payload.messageId)
        }
    }

    private fun handleReadMessage(content: String) {
        runCatching {
            val payload = gson.fromJson(content, MqttReadPayload::class.java)
            if (payload.roomId != currentRoomId || payload.readUserId == myUserId) return
            applyPartnerRead(payload.messageId)
        }
    }

    private fun applyPartnerRead(messageId: Long) {
        partnerReadMessageId = max(partnerReadMessageId, messageId)
        var changed = false
        messagesList.replaceAll { message ->
            if (message.isMyMessage && message.id > 0 && message.id <= partnerReadMessageId && message.isUnread) {
                changed = true
                message.copy(isUnread = false)
            } else {
                message
            }
        }
        if (changed) {
            _messages.postValue(messagesList.sortedBy { it.timestamp })
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
        object DeliveryDetail : Event()
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
        object ChatRoomNotificationOn : Event()
        object ChatRoomNotificationOff : Event()
        object ChatRoomBlocked : Event()
        object ChatRoomExited : Event()
        data class ChatUnavailable(val message: String) : Event()
    }

    private fun ChatMessageDto.toChatMessage(): ChatMessage {
        val messageType = resolveMessageType(mimetype, msg)
        val isSystem = send_uid == 0L || msg.isCompanionSupportNotice()
        return ChatMessage(
            id = msg_id,
            messagePointerId = msg_ptr_id,
            sender = send_uname.orEmpty().ifBlank { if (send_uid == 0L) "시스템" else send_uid.toString() },
            content = msg,
            messageType = messageType,
            messageImageUrl = if (messageType == MESSAGE_TYPE_IMAGE) msg.toDisplayMediaSource(mimetype) else null,
            messageVieoUrl = if (messageType == MESSAGE_TYPE_VIDEO) msg.toDisplayMediaSource(mimetype) else null,
            timestamp = send_date.toTimestamp(),
            isMyMessage = !isSystem && send_uid == myUserId,
            isUnread = !isSystem && send_uid == myUserId && msg_id > partnerReadMessageId,
            isSystem = isSystem
        )
    }

    private fun MqttMessagePayload.toChatMessage(category: String): ChatMessage {
        val messageType = resolveMessageType(mimeType, message, category)
        val resolvedMessageId = if (messageId > 0) messageId else System.currentTimeMillis()
        val isSystem = senderUserId == 0L || message.isCompanionSupportNotice()
        return ChatMessage(
            id = resolvedMessageId,
            messagePointerId = messagePointerId,
            sender = senderUserName.orEmpty().ifBlank {
                if (senderUserId == 0L) "시스템" else senderUserId.toString()
            },
            content = message,
            messageType = messageType,
            messageImageUrl = if (messageType == MESSAGE_TYPE_IMAGE) message.toDisplayMediaSource(mimeType) else null,
            messageVieoUrl = if (messageType == MESSAGE_TYPE_VIDEO) message.toDisplayMediaSource(mimeType) else null,
            timestamp = sendDate?.toTimestamp() ?: System.currentTimeMillis(),
            isMyMessage = !isSystem && senderUserId == myUserId,
            isUnread = !isSystem && senderUserId == myUserId && resolvedMessageId > partnerReadMessageId,
            isSystem = isSystem
        )
    }

    private fun String.isCompanionSupportNotice(): Boolean {
        return contains("동행지원하셨습니다") || contains("동행 지원하셨습니다")
    }

    private fun String.toFallbackMessageType(): Int {
        return when (this) {
            MqttChatCategory.ATTACH.topicName -> MESSAGE_TYPE_IMAGE
            else -> MESSAGE_TYPE_TEXT
        }
    }

    private fun resolveMessageType(mimeType: String, message: String, category: String? = null): Int {
        return when {
            mimeType.toMessageType(category) != MESSAGE_TYPE_TEXT -> mimeType.toMessageType(category)
            message.isAttachUrl() -> MESSAGE_TYPE_IMAGE
            category == MqttChatCategory.ATTACH.topicName -> MESSAGE_TYPE_IMAGE
            else -> MESSAGE_TYPE_TEXT
        }
    }

    private fun String.toMessageType(category: String? = null): Int {
        return when {
            startsWith("image/") -> MESSAGE_TYPE_IMAGE
            startsWith("video/") -> MESSAGE_TYPE_VIDEO
            category == MqttChatCategory.ATTACH.topicName && isBlank() -> MESSAGE_TYPE_IMAGE
            else -> MESSAGE_TYPE_TEXT
        }
    }

    private fun String.isAttachUrl(): Boolean {
        return (startsWith("http://") || startsWith("https://")) && contains("/attach/")
    }

    private fun String.toDisplayMediaSource(mimeType: String): String {
        if (startsWith("http://") || startsWith("https://") || startsWith("content://") || startsWith("data:")) {
            return this
        }
        return "data:$mimeType;base64,$this"
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
        private const val PENDING_MESSAGE_MATCH_WINDOW_MS = 30_000
    }
}
