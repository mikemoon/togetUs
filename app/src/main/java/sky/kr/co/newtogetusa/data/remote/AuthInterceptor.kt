package sky.kr.co.newtogetusa.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor@Inject constructor(private val tokenStore: TokenStore
) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.get()
        val builder = original.newBuilder()
        Timber.d("interceptor token $token")
        // 로그인/회원가입/토큰발급 API 등은 제외하고 싶다면 URL로 필터링
        if (!original.url.encodedPath.contains("/auth")) {
            token?.let { builder.header("Authorization", "Bearer $it") }
        }

        return chain.proceed(builder.build())
    }

    companion object{
        const val CODE_TOKEN = "TOKEN"
        const val CODE_AUTH = "AUTH"
    }
}