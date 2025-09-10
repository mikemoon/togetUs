package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

interface PlayerService {

    @POST("/api/players/v1")
    suspend fun postPlayer(
        @Body request: HashMap<String, String>
    ):Boolean


}