package sky.kr.co.newtogetusa.chat

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    @Provides
    @Singleton
    fun provideMessageCallback(
        //viewModel: ChatViewModel
    ): (String, String) -> Unit {
        return { sender, content ->
            // 메시지 수신 시 ViewModel에 전달
            //val message = ChatMessage(sender, content)
            //viewModel.addMessage(message)
        }
    }

    @Provides
    @Singleton
    fun provideAndroidMessageHandler(
        @ApplicationContext context: Context,
        messageCallback: (String, String) -> Unit
    ): MessageHandler {
        return AndroidMessageHandler(context, messageCallback)
    }
}