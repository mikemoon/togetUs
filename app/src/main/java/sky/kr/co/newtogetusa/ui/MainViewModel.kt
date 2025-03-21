package sky.kr.co.newtogetusa.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatClient: ChatClient
) : BaseViewModel() {

    fun connect() {
        chatClient.connect()
    }

    fun disconnect() {
        chatClient.disconnect()
    }

    fun sendMessage(message: String) {
        chatClient.sendMessage(message)
    }

    override fun onCleared() {
        super.onCleared()
        chatClient.disconnect()
    }
}