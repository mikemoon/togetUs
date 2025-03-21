package sky.kr.co.newtogetusa.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sky.kr.co.newtogetusa.chat.DefaultMqttConnectionConfig
import sky.kr.co.newtogetusa.chat.MqttConnectionConfig
import sky.kr.co.newtogetusa.di.NetworkModule.BrokerUrl
import sky.kr.co.newtogetusa.di.NetworkModule.ChatTopic
import sky.kr.co.newtogetusa.di.NetworkModule.Username
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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
}