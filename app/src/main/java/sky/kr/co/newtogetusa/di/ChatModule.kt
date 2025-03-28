package sky.kr.co.newtogetusa.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import sky.kr.co.newtogetusa.chat.AndroidMessageHandler
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.DefaultMqttConnectionConfig
import sky.kr.co.newtogetusa.chat.MessageCallbackManager
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.chat.MqttChatClientImpl
import sky.kr.co.newtogetusa.chat.MqttConnectionConfig
import sky.kr.co.newtogetusa.di.NetworkModule.BrokerUrl
import sky.kr.co.newtogetusa.di.NetworkModule.ChatTopic
import sky.kr.co.newtogetusa.di.NetworkModule.Username
import sky.kr.co.newtogetusa.ui.main.chat.ChattingTabViewModel
import timber.log.Timber
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChatModule {

    @Provides
    @Singleton
    @BrokerUrl
    fun provideBrokerUrl(): String = "tcp://broker.hivemq.com:1883"

    @Provides
    @Singleton
    @Username
    fun provideUsername(): String = "User-${System.currentTimeMillis() % 1000}"

    @Provides
    @Singleton
    @ChatTopic
    fun provideChatTopic(): String = "kotlin/mqtt/chat"

    @Provides
    @Singleton
    fun provideMqttConnectionConfig(
        @BrokerUrl brokerUrl: String,
        @Username username: String,
        @ChatTopic chatTopic: String
    ): MqttConnectionConfig {
        return DefaultMqttConnectionConfig(brokerUrl, username, chatTopic)
    }

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