package sky.kr.co.newtogetusa.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sky.kr.co.newtogetusa.BuildConfig
import sky.kr.co.newtogetusa.data.remote.api.KakaoNaviService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object KakaoNaviModule {

    /*@Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }*/

    @Provides @Singleton @NetworkModule.KakaoNaviAuth
    fun provideAuthInterceptor(): okhttp3.Interceptor = okhttp3.Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK " + "5ce32dc200f3f071017e7a921ae50912")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(req)
    }

    @Provides @Singleton @NetworkModule.KakaoNaviClient
    fun provideKakaoNaviOkHttp(
        @NetworkModule.KakaoNaviAuth auth: Interceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(logging)
        .build()

    @Provides @Singleton @NetworkModule.KakaoNaviRetrofit
    fun provideKakaoNaviRetrofit(
        @NetworkModule.KakaoNaviClient client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://apis-navi.kakaomobility.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideKakaoNaviService(
        @NetworkModule.KakaoNaviRetrofit retrofit: Retrofit
    ): KakaoNaviService = retrofit.create(KakaoNaviService::class.java)

}