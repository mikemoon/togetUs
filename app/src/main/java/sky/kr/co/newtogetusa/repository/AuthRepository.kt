package sky.kr.co.newtogetusa.repository

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.APiService
import sky.kr.co.newtogetusa.data.remote.dto.ChangeEmailPasswordResponse
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.di.NetworkModule
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @NetworkModule.ApiServer private val apiService: APiService,
    @ApplicationContext private val context: Context
): BaseNetRepo() {

    suspend fun verifyEmail(requestBody: HashMap<String, String>) = safeApiCall<Boolean>(dispatcher = Dispatchers.IO) {
        apiService.verifyEmail(requestBody)
    }

    suspend fun verifyAccount(token: String, requestBody: HashMap<String, String>) =
        safeApiCall(dispatcher = Dispatchers.IO) {
            apiService.verifyAccount("Bearer $token", requestBody)
        }

    suspend fun cerifyVerifyCode(requestBody: HashMap<String, String>) = safeApiCall<Boolean>(dispatcher = Dispatchers.IO) {
        apiService.certifyVerifyCode(requestBody)
    }

    suspend fun changeEmailPassword(requestBody: HashMap<String, String>) = safeApiCall<ChangeEmailPasswordResponse>(dispatcher = Dispatchers.IO){
        apiService.changeEmailPassword(requestBody)
    }

    suspend fun join(requestBody: HashMap<String, Any>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.join(requestBody)
    }

    suspend fun loginFromEmail(requestBody: HashMap<String, String>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.loginFromEmail(requestBody.withDeviceInfo())
    }

    suspend fun loginKakao(requestBody: HashMap<String, String>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.loginKakao(requestBody.withDeviceInfo())
    }

    suspend fun loginNaver(requestBody: HashMap<String, String>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.loginNaver(requestBody.withDeviceInfo())
    }

    suspend fun loginGoogle(requestBody: HashMap<String, String>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.loginGoogle(requestBody.withDeviceInfo())
    }

    suspend fun checkNickname(nickname:String) = safeApiCall<Boolean>(dispatcher = Dispatchers.IO){
        apiService.checkNickname(nickname)
    }

    suspend fun getTerms() = safeApiCall<List<TermMeta>>(Dispatchers.IO){
        apiService.getTerms()
    }

    private fun HashMap<String, String>.withDeviceInfo(): HashMap<String, String> = apply {
        put("os", "A")
        put("device_id", deviceId())
        put("platform", "AOS")
    }

    private fun deviceId(): String {
        val prefs = context.getSharedPreferences(DEVICE_PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_DEVICE_ID, null)?.let { return it }

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val id = androidId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        return id
    }

    private companion object {
        const val DEVICE_PREFS = "device"
        const val KEY_DEVICE_ID = "device_id"
    }
}
