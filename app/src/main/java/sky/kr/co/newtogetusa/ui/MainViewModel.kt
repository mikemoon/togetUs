package sky.kr.co.newtogetusa.ui

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val messageCallbackManager: MessageCallbackManager,
    private val tokenStore: TokenStore,
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val configRepository: ConfigRepository
) : BaseViewModel(baseViewModelFactory.create()), MessageHandler {

    val isModeChanging = MutableStateFlow(false)
    val isPlayerModeFlow = MutableStateFlow(false)
    val hasUnreadChatFlow = MutableStateFlow(false)

    init {
        messageCallbackManager.registerCallback(this)

        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                .collectLatest { isPlayerMode ->
                    isPlayerModeFlow.value = isPlayerMode
                }
        }
    }



    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            chatClient.connect()
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            chatClient.disconnect()
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatClient.sendMessage(message)
        }
    }

    fun clearChatUnread() {
        hasUnreadChatFlow.value = false
    }

    fun postFCMToken(token: String) = viewModelScope.launch {
        dataStoreRepository.putString(DataStoreKey.KEY_FCM_TOKEN, token)
        when(val res = configRepository.postPushToken(hashMapOf("device_token" to token, "device_type" to "AOS"))){
            is ResultWrapper.Success -> {
            }
            else -> {
                Timber.e("fcm register error ${res}")
            }
        }

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

    fun clearSessionData() = viewModelScope.launch {
        tokenStore.clear()
        dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, "")
        dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, "")
    }

    override fun onCleared() {
        super.onCleared()
        messageCallbackManager.unregisterCallback(this)
        chatClient.disconnect()
    }

    override fun handleIncomingMessage(sender: String, content: String) {
        hasUnreadChatFlow.value = true
    }

    override fun onConnectionLost(cause: Throwable?) {
        Timber.d(cause, "MQTT connection lost on main")
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) = Unit
}
