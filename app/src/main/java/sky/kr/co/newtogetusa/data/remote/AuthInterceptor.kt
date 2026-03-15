package sky.kr.co.newtogetusa.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import sky.kr.co.newtogetusa.auth.AuthSessionManager
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.data.remote.api.RefreshApi
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi,
    private val authSessionManager: AuthSessionManager
) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        // 1) 인증 헤더 부착 (auth 계열/재발급 호출에는 붙이지 않음)
        val builder = original.newBuilder()
        val isAuthCall = path.startsWith("/auths")
        if (!isAuthCall) {
            tokenStore.getAccessToken()?.let { builder.header("Authorization", "Bearer $it") }
        }

        var response = chain.proceed(builder.build())

        // 2) 401 → refresh 시도 (재발급/회원/로그인 API 등은 제외)
        Timber.d("authIntercept ${response.code}")
        if (response.code == 401 && !isAuthCall) {
            val refreshed = refreshTokensBlocking()
            if (refreshed != null) {
                // 재시도할 거니까 '지금 응답' 닫고 진행
                response.close()

                tokenStore.setTokens(refreshed.accessToken, refreshed.refreshToken)
                val retry = original.newBuilder()
                    .removeHeader("Authorization")
                    .apply { tokenStore.getAccessToken()?.let { header("Authorization", "Bearer $it") } }
                    .build()
                return chain.proceed(retry)
            } else {
                tokenStore.clear()
                authSessionManager.notifySessionExpired()
                // 재시도 안 함: 현재 response를 '닫지 않고' 그대로 반환
                return response
            }
        }
        return response
    }

    /** Interceptor는 suspend 불가 → 동기 execute() 사용 */
    private fun refreshTokensBlocking(): JoinResponse? {
        val refresh = tokenStore.getRefreshToken() ?: return null
        return try {
            val body = hashMapOf("refreshToken" to refresh)
            val call = refreshApi.reissue(body)
            val resp = call.execute()
            if (resp.isSuccessful) resp.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}