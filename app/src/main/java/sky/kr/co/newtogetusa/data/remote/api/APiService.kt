package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import sky.kr.co.newtogetusa.data.remote.dto.BaseResponse
import sky.kr.co.newtogetusa.data.remote.dto.ChangeEmailPasswordResponse
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse

interface APiService {

    @POST("/auths/join")
    suspend fun join(
        @Body body: HashMap<String, Any>
    ):JoinResponse

    @PATCH("/auths/email/verify")
    suspend fun verifyEmail(
        @Body body: HashMap<String, String>
    ):Boolean

    @PATCH("/auths/email/certify")
    suspend fun certifyVerifyCode(
        @Body body: HashMap<String, String>
    ):Boolean

    @PATCH("/auths/email/change_pw")
    suspend fun changeEmailPassword(
        @Body body: HashMap<String, String>
    ):ChangeEmailPasswordResponse


}