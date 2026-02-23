package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.BaseDto
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
    ): List<BaseDto>

    @GET("/api/configs/v1/delivery/status")
    suspend fun getDeliveryStatus(
    ):List<TermMeta>

    @GET("/api/configs/v1/delivery/report")//배송신고유형코드
    suspend fun getReport(
    ):List<TermMeta>

    @GET("/api/configs/v1/delivery/block")//배송차단코드
    suspend fun getBlock(
    ):List<TermMeta>

    @GET("/api/configs/v1/delivery/status")//배송상태코드
    suspend fun getStatus(
    ):Any

    @GET("/api/configs/v1/delivery/product_weight")//물품 무게코드
    suspend fun getWeight(
    ):List<BaseCommonDto>

    @GET("/api/configs/v1/delivery/product_volume")//물품부피코드
    suspend fun getProductVolume(
    ):List<BaseCommonDto>

    @GET("/api/configs/v1/delivery/product_type")//물품종류
    suspend fun getProductType(
    ):List<BaseCommonDto>

    @GET("/api/configs/v1/review/user")//사용자리뷰를 위한 평가항목
    suspend fun getUserReview(
    ):Any

    @GET("/api/configs/v1/review/player")//플레이어리뷰를 위한 평가항목
    suspend fun getPlayerReview(

    ):Any


}