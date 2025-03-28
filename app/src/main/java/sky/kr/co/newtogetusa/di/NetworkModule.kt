package sky.kr.co.newtogetusa.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import sky.kr.co.newtogetusa.data.remote.api.AuthService
import javax.inject.Qualifier
import javax.inject.Singleton

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

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiServer

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiOkHttpClient

    /*@ApiServer
    @Provides
    fun provideApiUrl()= if(BuildConfig.DEBUG){
        ""
    }else{
        ""
    }*/

    @ApiServer
    @Singleton
    @Provides
    fun provideAuthService(@ApiServer retrofit: Retrofit): AuthService{
        return  retrofit.create(AuthService::class.java)
    }
}