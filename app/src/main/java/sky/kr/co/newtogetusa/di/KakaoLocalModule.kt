package sky.kr.co.newtogetusa.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
import sky.kr.co.newtogetusa.data.remote.api.KakaoLocalService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object KakaoLocalModule {

    private const val BASE_URL = "https://dapi.kakao.com/"

    @Provides @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides @Singleton @NetworkModule.KakaoLocalAuth
    fun provideAuthHeaderInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK " + "5ce32dc200f3f071017e7a921ae50912")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(req)
    }

    /*@Provides @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }*/

    @Provides @Singleton @NetworkModule.KakaoLocalClient
    fun provideKakaoLocalOkHttp(
        @NetworkModule.KakaoLocalAuth auth: Interceptor,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton @NetworkModule.KakaoLocalRetrofit
    fun provideKakaoLocalRetrofit(
        gson: Gson,
        @NetworkModule.KakaoLocalClient client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides @Singleton
    fun provideKakaoLocalService(
        @NetworkModule.KakaoLocalRetrofit retrofit: Retrofit
    ): KakaoLocalService = retrofit.create(KakaoLocalService::class.java)
}