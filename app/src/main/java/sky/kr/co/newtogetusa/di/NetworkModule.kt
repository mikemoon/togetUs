package sky.kr.co.newtogetusa.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BrokerUrl

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Username

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ChatTopic
}