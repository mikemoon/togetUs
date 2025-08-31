package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto

interface ConfigService {

    @GET("/api/configs/v1/areas/domestic")
    suspend fun getDomesticAreas(
    ): List<RegionDto>

    @GET("/api/configs/v1/areas/domestic/gu")
    suspend fun getDomesticSubAreas(
    ): List<RegionDto>
}