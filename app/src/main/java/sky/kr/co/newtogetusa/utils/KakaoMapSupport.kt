package sky.kr.co.newtogetusa.utils

import timber.log.Timber

object KakaoMapSupport {
    @Volatile
    var isAvailable: Boolean = false
        private set

    fun markAvailable() {
        isAvailable = true
    }

    fun markUnavailable(error: Throwable) {
        isAvailable = false
        Timber.e(error, "KakaoMapSdk is unavailable on this device")
    }
}
