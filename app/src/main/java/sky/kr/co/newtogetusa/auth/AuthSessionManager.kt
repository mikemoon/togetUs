package sky.kr.co.newtogetusa.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor() {
    private val _sessionExpiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredFlow = _sessionExpiredFlow.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpiredFlow.tryEmit(Unit)
    }
}