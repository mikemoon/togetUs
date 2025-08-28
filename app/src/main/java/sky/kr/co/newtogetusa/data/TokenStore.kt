package sky.kr.co.newtogetusa.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor() {
    @Volatile private var token: String? = null
    fun set(t: String?) { token = t }
    fun get(): String? = token
}