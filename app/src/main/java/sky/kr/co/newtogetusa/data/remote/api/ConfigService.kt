package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header

interface ConfigService {

    @GET("/api/configs/v1/areas/domestic")
    suspend fun getDomesticAreas(
    ): Boolean
}