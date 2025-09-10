package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import sky.kr.co.newtogetusa.data.remote.dto.BaseResponse
import sky.kr.co.newtogetusa.data.remote.dto.ChangeEmailPasswordResponse
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta

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

    @PATCH("/auths/email")
    suspend fun loginFromEmail(
        @Body body: HashMap<String, String>
    ):JoinResponse

    @POST("/auths/kakao")
    suspend fun loginKakao(
        @Body body: HashMap<String, String>
    ): JoinResponse

    @POST("/auths/naver")
    suspend fun loginNaver(
        @Body body: HashMap<String, String>
    ): JoinResponse

    @POST("/auths/google")
    suspend fun loginGoogle(
        @Body body: HashMap<String, String>
    ): JoinResponse

    @GET("/auths/join/check_dup")
    suspend fun checkNickname(
        @Query("nickname") nickname:String
    ):Boolean

    @GET("/auths/join/terms")
    suspend fun getTerms(
    ): List<TermMeta>


}