package sky.kr.co.newtogetusa.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatClient: ChatClient,
    baseViewModelFactory: BaseViewModelDependenciesFactory
) : BaseViewModel(baseViewModelFactory.create()) {

    val isModeChanging = MutableStateFlow(false)
    val isPlayerModeFlow = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                .collectLatest { isPlayerMode ->
                    isPlayerModeFlow.value = isPlayerMode
                }
        }
    }



    fun connect() {
        chatClient.connect()
    }

    fun disconnect() {
        chatClient.disconnect()
    }

    fun sendMessage(message: String) {
        chatClient.sendMessage(message)
    }

    fun onModeChange(isPlayerMode: Boolean) {
        isModeChanging.value = true
        viewModelScope.launch {
            delay(3000)
            dataStoreRepository.putBoolean(DataStoreKey.KEY_IS_MODE_PLAYER, isPlayerMode)
            isPlayerModeFlow.value = isPlayerMode
            isModeChanging.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatClient.disconnect()
    }
}