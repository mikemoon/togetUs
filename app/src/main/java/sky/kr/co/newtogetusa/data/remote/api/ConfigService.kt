package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.BaseDto
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.data.remote.dto.config.NotificationSettingDto
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.data.remote.request.config.NotificationSettingReq

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

    ):List<BaseCommonDto>

    @POST("/api/mys/v1/push/device") //디바이스 토큰 등록
    suspend fun postPushToken(
        @Body body: HashMap<String, String>
    ): Boolean

    @HTTP(method = "DELETE", hasBody = true, path = "/api/mys/v1/push/device") //디바이스 토큰 삭제
    suspend fun deletePushToken(
        @Body body: HashMap<String, Any?>
    ): Boolean

    @GET("/api/mys/v1/push/setting")//알림 설정 조회
    suspend fun getAlarmSettings(
    ): NotificationSettingDto

    @PUT("/api/mys/v1/push/setting")
    suspend fun putAlarmSettings(
        @Body body: NotificationSettingReq
    ): Boolean

}
