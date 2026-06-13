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
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi,
    private val authSessionManager: AuthSessionManager,
    private val dataStoreRepository: DataStoreRepository
) : Interceptor {

    companion object {
        private val refreshLock = Any()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath
        val requestAccessToken = getAccessTokenBlocking()

        // 1) 인증 헤더 부착 (auth 계열/재발급 호출에는 붙이지 않음)
        val builder = original.newBuilder()
        val isAuthCall = path.startsWith("/auths")
        if (!isAuthCall) {
            requestAccessToken?.let { builder.header("Authorization", "Bearer $it") }
        }

        var response = chain.proceed(builder.build())

        // 2) 401 → refresh 시도 (재발급/회원/로그인 API 등은 제외)
        Timber.d("authIntercept ${response.code}")
        if (response.code == 401 && !isAuthCall) {
            synchronized(refreshLock) {
                val currentAccessToken = tokenStore.getAccessToken()
                val refreshedByOtherRequest =
                    !currentAccessToken.isNullOrBlank() && currentAccessToken != requestAccessToken

                val accessTokenForRetry = if (refreshedByOtherRequest) {
                    currentAccessToken
                } else {
                    refreshTokensBlocking()?.accessToken
                }

                if (!accessTokenForRetry.isNullOrBlank()) {
                    // 재시도할 거니까 '지금 응답' 닫고 진행
                    response.close()

                    val retry = original.newBuilder()
                        .removeHeader("Authorization")
                        .header("Authorization", "Bearer $accessTokenForRetry")
                        .build()
                    return chain.proceed(retry)
                } else {
                    clearTokensBlocking()
                    authSessionManager.notifySessionExpired()
                    // 재시도 안 함: 현재 response를 '닫지 않고' 그대로 반환
                    return response
                }
            }
        }
        return response
    }

    private fun getAccessTokenBlocking(): String? {
        tokenStore.getAccessToken()?.takeIf { it.isNotBlank() }?.let { return it }
        return runBlocking {
            dataStoreRepository.getString(DataStoreKey.KEY_TOKEN)
                ?.takeIf { it.isNotBlank() }
                ?.also { tokenStore.setAccessToken(it) }
        }
    }

    /** Interceptor는 suspend 불가 → 동기 execute() 사용 */
    private fun refreshTokensBlocking(): JoinResponse? {
        val refresh = getRefreshTokenBlocking() ?: return null
        return try {
            val body = hashMapOf("refreshToken" to refresh)
            val call = refreshApi.reissue(body)
            val resp = call.execute()
            if (resp.isSuccessful) {
                resp.body()?.also { refreshed ->
                    tokenStore.setTokens(refreshed.accessToken, refreshed.refreshToken)
                    runBlocking {
                        dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, refreshed.accessToken)
                        dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, refreshed.refreshToken)
                        dataStoreRepository.clearString(DataStoreKey.KEY_PROFILE)
                    }
                }
            } else {
                Timber.w("reissue failed: code=${resp.code()} body=${resp.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "reissue exception")
            null
        }
    }

    private fun getRefreshTokenBlocking(): String? {
        tokenStore.getRefreshToken()?.takeIf { it.isNotBlank() }?.let { return it }
        return runBlocking {
            dataStoreRepository.getString(DataStoreKey.KEY_REFRESH_TOKEN)
                ?.takeIf { it.isNotBlank() }
                ?.also { tokenStore.setRefreshToken(it) }
        }
    }

    private fun clearTokensBlocking() {
        tokenStore.clear()
        runBlocking {
            dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, "")
            dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, "")
            dataStoreRepository.clearString(DataStoreKey.KEY_PROFILE)
        }
    }
}
