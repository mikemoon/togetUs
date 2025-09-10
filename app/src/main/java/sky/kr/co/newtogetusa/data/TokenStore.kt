package sky.kr.co.newtogetusa.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor() {
    @Volatile private var accessToken: String? = null
    @Volatile private var refreshToken: String? = null

    private val lock = Any()

    fun setAccessToken(t: String?) = synchronized(lock) { accessToken = t }
    fun setRefreshToken(t: String?) = synchronized(lock) { refreshToken = t }

    fun getAccessToken(): String? = accessToken
    fun getRefreshToken(): String? = refreshToken

    fun setTokens(access: String?, refresh: String?) = synchronized(lock) {
        accessToken = access; refreshToken = refresh
    }

    fun clear() = synchronized(lock) {
        accessToken = null; refreshToken = null
    }
}
