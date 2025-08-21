package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.APiService
import sky.kr.co.newtogetusa.data.remote.dto.ChangeEmailPasswordResponse
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @NetworkModule.ApiServer private val apiService: APiService
): BaseNetRepo() {

    suspend fun verifyEmail(requestBody: HashMap<String, String>) = safeApiCall<Boolean>(dispatcher = Dispatchers.IO) {
        apiService.verifyEmail(requestBody)
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
        apiService.loginFromEmail(requestBody)
    }

    suspend fun loginKakao(requestBody: HashMap<String, String>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.loginKakao(requestBody)
    }

    suspend fun loginNaver(requestBody: HashMap<String, String>) = safeApiCall<JoinResponse>(dispatcher = Dispatchers.IO){
        apiService.loginNaver(requestBody)
    }
}