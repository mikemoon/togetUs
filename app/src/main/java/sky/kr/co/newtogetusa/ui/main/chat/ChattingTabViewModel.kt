package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ChattingTabViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val callbackManager: MessageCallbackManager
) : BaseViewModel(), MessageHandler {

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
        disconnect()
        callbackManager.unregisterCallback(this)
    }

    override fun handleIncomingMessage(sender: String, content: String) {
        TODO("Not yet implemented")
    }

    override fun onConnectionLost(cause: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
        TODO("Not yet implemented")
    }
}