package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.MqttChatCategory
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.chat.ChattingPlayerViewModel.Event
import javax.inject.Inject

@HiltViewModel
class ChattingConversationViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val callbackManager: MessageCallbackManager,
    baseViewModelFactory: BaseViewModelDependenciesFactory
) : BaseViewModel(baseViewModelFactory.create()), MessageHandler {

    init {
        callbackManager.registerCallback(this)
    }

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val messagesList = mutableListOf<ChatMessage>()

    // 메시지 핸들러 콜백
    val messageCallback: (String, String) -> Unit = { sender, content ->
        //val isMyMessage = sender == chatClient.getUsername()
        //val message = ChatMessage(sender, content, isMyMessage = isMyMessage)
        //addMessage(message)
    }

    init {
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
        if (content.isBlank()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatClient.sendMessage(content)
            }
            addMessage(
                ChatMessage(
                    id = System.currentTimeMillis(),
                    sender = "me",
                    content = content,
                    messageType = 0,
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
            messagesList.add(message)
            _messages.value = messagesList.toList()
        }
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    override fun onCleared() {
        super.onCleared()
        callbackManager.unregisterCallback(this)
    }

    override fun handleIncomingMessage(sender: String, content: String) {
        val messageType = when (sender) {
            MqttChatCategory.ATTACH.topicName -> 1
            MqttChatCategory.GPS.topicName -> 3
            else -> 0
        }
        addMessage(
            ChatMessage(
                id = System.currentTimeMillis(),
                sender = sender,
                content = content,
                messageType = messageType,
                isMyMessage = false
            )
        )
    }

    override fun onConnectionLost(cause: Throwable?) {
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
        // Delivery acknowledgement is currently reflected by optimistic UI append.
    }

    val sendMessageEnable = MutableStateFlow(false)
    fun onChattingMessage(s: CharSequence, start: Int, before: Int, count: Int) {
        sendMessageEnable.value = s.isNotEmpty()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> get() = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object PhoneCall : Event()
        object More : Event()
        data class MessageImageSelect(val url:String) : Event()
        data class MessageVideoSelect(val url:String) : Event()
        data class MessageResend(val id:Long):Event()
        data class MessageDelete(val id:Long):Event()
        object InputMore : Event()
        object InputSend : Event()
        object InputCamera : Event()
        object InputAlbum : Event()
        object InputMovie : Event()
    }
}
