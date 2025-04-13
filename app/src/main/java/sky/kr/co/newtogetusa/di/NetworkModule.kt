package sky.kr.co.newtogetusa.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sky.kr.co.newtogetusa.BuildConfig
import sky.kr.co.newtogetusa.data.remote.api.AddressSearchService
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

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AddressApiServer

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AddressOkHttpClient

    @AddressApiServer
    @Provides
    fun provideAddressApiUrl()= "https://dapi.kakao.com/"

    @AddressOkHttpClient
    @Singleton
    @Provides
    fun provideAddressApiService(): OkHttpClient{
        val logger = HttpLoggingInterceptor().apply {
            level = if(BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(logger)
        return okHttpClientBuilder.build()
    }

    @AddressApiServer
    @Singleton
    @Provides
    fun provideAddressRetrofit(@AddressOkHttpClient client: OkHttpClient, @AddressApiServer url: String):Retrofit{
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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

    @AddressApiServer
    @Singleton
    @Provides
    fun provideAddressService(@AddressApiServer retrofit: Retrofit): AddressSearchService{
        return retrofit.create(AddressSearchService::class.java)
    }
}