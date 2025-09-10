package sky.kr.co.newtogetusa.di

import com.kakao.sdk.auth.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sky.kr.co.newtogetusa.BuildConfig
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.data.remote.AuthInterceptor
import sky.kr.co.newtogetusa.data.remote.api.APiService
import sky.kr.co.newtogetusa.data.remote.api.AddressSearchService
import sky.kr.co.newtogetusa.data.remote.api.ConfigService
import sky.kr.co.newtogetusa.data.remote.api.DirectionsApiService
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.data.remote.api.RefreshApi
import sky.kr.co.newtogetusa.data.remote.api.UserService
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
    annotation class ConfigApi

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class UserApi

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PlayerApi

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AddressApiServer

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AddressOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KakaoLocalOkHttp

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class KakaoLocalAuth

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class KakaoNaviAuth

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class KakaoLocalClient

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class KakaoNaviClient

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class KakaoLocalRetrofit

    @Qualifier @Retention(AnnotationRetention.BINARY)
    annotation class KakaoNaviRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RefreshOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RefreshRetrofit

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

    @Provides
    fun provideDirectionsApi(): DirectionsApiService {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
    }

    @ApiServer
    @Provides
    fun provideApiUrl()= if(BuildConfig.DEBUG){
        "http://togetus.p-e.kr/"
    }else{
        "http://togetus.p-e.kr/"
    }

    @ApiOkHttpClient
    @Singleton
    @Provides
    fun provideApiOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logger)
        return  okHttpClientBuilder.build()
    }

    @ApiServer
    @Singleton
    @Provides
    fun provideRetrofit(@ApiOkHttpClient client: OkHttpClient, @ApiServer url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @ApiServer
    @Singleton
    @Provides
    fun provideApiService(@ApiServer retrofit: Retrofit): APiService {
        return retrofit.create(APiService::class.java)
    }

    @AddressApiServer
    @Singleton
    @Provides
    fun provideAddressService(@AddressApiServer retrofit: Retrofit): AddressSearchService{
        return retrofit.create(AddressSearchService::class.java)
    }

    @ConfigApi
    @Singleton
    @Provides
    fun provideConfigService(@ApiServer retrofit: Retrofit): ConfigService {
        return retrofit.create(ConfigService::class.java)
    }

    @UserApi
    @Singleton
    @Provides
    fun provideUserService(@ApiServer retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @PlayerApi
    @Singleton
    @Provides
    fun providePlayerService(@ApiServer retrofit: Retrofit): PlayerService {
        return retrofit.create(PlayerService::class.java)
    }

    @Provides @Singleton
    @RefreshOkHttpClient
    fun provideRefreshOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)           // ✅ 리프레시 호출 로그가 여기서 찍힙니다
            // 절대 AuthInterceptor 추가 금지!
            .build()
    }

    @Provides @Singleton
    @RefreshRetrofit
    fun provideRefreshRetrofit(
        @ApiServer baseUrl: String,
        @RefreshOkHttpClient client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)                  // "http://togetus.p-e.kr/"  (옵션 A 기준)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton
    fun provideRefreshApi(
        @RefreshRetrofit retrofit: Retrofit
    ): RefreshApi = retrofit.create(RefreshApi::class.java)
}