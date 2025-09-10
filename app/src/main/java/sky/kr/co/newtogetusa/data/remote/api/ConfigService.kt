package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto

interface ConfigService {

    @GET("/api/configs/v1/areas/domestic")
    suspend fun getDomesticAreas(
    ): List<RegionDto>

    @GET("/api/configs/v1/areas/domestic/gu")
    suspend fun getDomesticSubAreas(
    ): List<RegionDto>

    @GET("/api/configs/v1/areas/overseas")
    suspend fun getOverseas(
    ): List<RegionDto>

    @GET("/api/configs/v1/player/terms")
    suspend fun getPlayerTerms(
    ): List<TermMeta>

    @GET("/api/configs/v1/player/banks")
    suspend fun getBanks(
    ): List<TermMeta>

    @GET("/api/configs/v1/delivery/status")
    suspend fun getDeliveryStatus(
    ):List<TermMeta>

    @GET("/api/configs/v1/delivery/report")
    suspend fun getReport(
    ):List<TermMeta>

    @GET("/api/configs/v1/delivery/block")
    suspend fun getBlock(
    ):List<TermMeta>

}