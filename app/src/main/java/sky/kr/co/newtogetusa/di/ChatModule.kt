package sky.kr.co.newtogetusa.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.chat.MqttChatClientImpl
import sky.kr.co.newtogetusa.chat.MqttConnectionConfig
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChatModule {

    @Provides
    @Singleton
    fun provideChatClient(messageHandler: MessageHandler, mqttConnectionConfig: MqttConnectionConfig): ChatClient {
        return MqttChatClientImpl(mqttConnectionConfig, messageHandler)
    }


    /*@Provides
    @Singleton
    fun provideMessageCallback(
        viewModel: Provider<ChattingTabViewModel>
    ): (String, String) -> Unit {
        return { sender, content ->
            // 메시지 수신 시 ViewModel에 전달
            //val message = ChatMessage(sender, content)
            //viewModel.addMessage(message)
            Timber.d("Message received: $sender, $content")
        }
    }*/

    @Provides
    @Singleton
    fun provideAndroidMessageHandler(
        @ApplicationContext context: Context,
        callbackManager: MessageCallbackManager
    ): MessageHandler {
        return object : MessageHandler {
            override fun handleIncomingMessage(sender: String, content: String) {
                callbackManager.notifyMessageReceived(sender, content)
            }

            override fun onConnectionLost(cause: Throwable?) {
                Timber.d("MQTT 연결 끊김: ${cause?.message}")
                callbackManager.notifyConnectionLost(cause)
            }

            override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
                callbackManager.notifyDeliveryComplete(token)
            }
        }
    }
}