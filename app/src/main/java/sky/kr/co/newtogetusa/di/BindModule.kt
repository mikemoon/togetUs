package sky.kr.co.newtogetusa.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.chat.ConsoleMessageHandler
import sky.kr.co.newtogetusa.chat.MessageHandler
import sky.kr.co.newtogetusa.chat.MqttChatClientImpl
import javax.inject.Singleton

/*
@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {
    @Binds
    @Singleton
    abstract fun bindMessageHandler(
        consoleMessageHandler: ConsoleMessageHandler
    ): MessageHandler

    @Binds
    @Singleton
    abstract fun bindChatClient(
        mqttChatClientImpl: MqttChatClientImpl
    ): ChatClient
}*/
