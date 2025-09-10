package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import sky.kr.co.newtogetusa.data.remote.dto.JoinResponse

interface RefreshApi {
    @POST("/auths/reissue")
    fun reissue(@Body body: HashMap<String, String>): retrofit2.Call<JoinResponse>
}