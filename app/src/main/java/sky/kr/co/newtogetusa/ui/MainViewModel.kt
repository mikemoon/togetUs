package sky.kr.co.newtogetusa.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatClient: ChatClient,
    baseViewModelFactory: BaseViewModelDependenciesFactory
) : BaseViewModel(baseViewModelFactory.create()) {

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