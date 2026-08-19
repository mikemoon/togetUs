package sky.kr.co.newtogetusa.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.CommonBoolDto
import sky.kr.co.newtogetusa.data.remote.dto.CommonIntDto
import sky.kr.co.newtogetusa.data.remote.dto.player.DeliveryCalendarDayResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.player.DeliveryCalendarResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.player.DeliveryHistoryResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerApplyedInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PortOneConfigDto
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerSearchResponse
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddedRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerDeliveryHistoryReq
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaDeleteRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerJoinRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerSearchRequest

interface PlayerService {

    @Multipart
    @POST("/api/players/v1/{player_id}/profile_image")//프로필사진 등록
    suspend fun postProfileImage(
        @Path("player_id") playerId: Int,
        @Part file: MultipartBody.Part
    ):Boolean

    @POST("/api/players/v1/{player_id}/profile_image")
    suspend fun postProfileImage(
        @Path("player_id") playerId: Int,
        @Body request: PlayerProfileImageRequest
    ):Boolean

    @POST("/api/players/v1/{player_id}/introduction")//자기소개등록
    suspend fun postIntroduction(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @Multipart
    @POST("/api/players/v1/{player_id}/criminal_record")
    suspend fun postCriminalRecord(
        @Path("player_id") playerId: Int,
        @Part file: MultipartBody.Part
    ):Boolean

    @GET("/api/players/v1/{player_id}/profile")
    suspend fun getProfile(
        @Path("player_id") playerId: Int
    ): PlayerProfileDto

    @POST("/api/players/v1/{player_id}/report")
    suspend fun postReportPlayer(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ): PlayerInfoDto

    @POST("/api/players/v1/{player_id}/block")
    suspend fun postBlockPlayer(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ): PlayerInfoDto

    @POST("/api/players/v1/{player_id}/unblock")
    suspend fun unblockPlayer(
        @Path("player_id") playerId: Int
    ): CommonBoolDto

    @GET("/api/players/v1/blocks")
    suspend fun getBlockedPlayers(): PlayerSearchResponse

    @GET("/api/players/v1/likes")
    suspend fun getPlayerLikes(): PlayerSearchResponse

    @POST("/api/players/v1/{player_id}/req_delivery") //배송요청 제안
    suspend fun postReqDelivery(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ): PlayerInfoDto

    @POST("/api/players/v1/{player_id}/like")
    suspend fun postLikePlayer(
        @Path("player_id") playerId: Int
    ): Boolean

    @POST("/api/players/v1/{player_id}/unlike")
    suspend fun postUnlikePlayer(
        @Path("player_id") playerId: Int
    ): Boolean

    @GET("/api/players/v1/{player_id}/reviews")
    suspend fun getReviews(
        @Path("player_id") playerId: Int
    ):List<PlayerInfoDto>

    @POST("/api/players/v1/{player_id}/reviews")
    suspend fun postReview(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @GET("/api/players/v1")//플레이어 신청중 현황
    suspend fun getPlayers(
    ): PlayerApplyedInfoDto

    @POST("/api/players/v1")//플레이어 신청 등록
    suspend fun postPlayer(
        @Body request: HashMap<String, String>
    ):Boolean

    @POST("/api/players/v1/{player_id}/introduction")
    suspend fun postPlayerIntroduction(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @POST("/api/players/v1/{player_id}/bank")
    suspend fun postPlayerBank(
        @Path("player_id") playerId: Int,
        @Body request: BankRequestDto
    )

    @POST("/api/players/v1/{player_id}/area") // 배송가능지역 추가하기
    suspend fun postPlayerArea(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ): Int

    @POST("/api/players/v1/{player_id}/area")
    suspend fun postPlayerArea(
        @Path("player_id") playerId: Int,
        @Body request: PlayerAreaAddRequest
    ): Int

    @HTTP(method = "DELETE", path = "/api/players/v1/{player_id}/area", hasBody = true)
    suspend fun deletePlayerArea(
        @Path("player_id") playerId: Int,
        @Body request: PlayerAreaDeleteRequest
    )

    @POST("/api/players/v1/{player_id}/area/gps_enable")
    suspend fun setPlayerGpsEnable(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, Boolean>
    ): Boolean

    @POST("/api/players/v1/{player_id}/area_enable")
    suspend fun setPlayerAreaEnable(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, Any>
    ): Boolean

    // iOS 대응: 서버는 배열 [{area_id, enable}, ...]로 한 번에 받아 처리한다 (단건 객체 전송 시 400)
    @POST("/api/players/v1/{player_id}/area_enable")
    suspend fun setPlayerAreaEnableBatch(
        @Path("player_id") playerId: Int,
        @Body request: List<HashMap<String, Any>>
    ): Boolean

    @POST("/api/players/v1/{player_id}/area/gps_refresh")
    suspend fun refreshPlayerGps(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, Double>
    ): CommonIntDto

    @POST("/api/players/v1/{player_id}/area/added")
    suspend fun postPlayerAreaAdded(
        @Path("player_id") playerId: Int,
        @Body request: PlayerAreaAddedRequest
    ): Int

    @POST("/api/players/v1/{player_id}/apply") //등록심사 요청
    suspend fun postPlayerApply(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    )

    @POST("/api/players/v1/{player_id}/cancel_apply") //플레이어 신청 취소
    suspend fun cancelPlayerApplication(
        @Path("player_id") playerId: Int
    ): Boolean

    @Multipart
    @POST("/api/players/v1/{player_id}/apply_batch") //등록심사 일괄요청
    suspend fun postPlayerApplyBatch(
        @Path("player_id") playerId: Int,
        @Part("data") data: RequestBody,
        @Part profile_image: MultipartBody.Part?,
        @Part criminal_record: MultipartBody.Part?
    )

    @GET("/api/players/v1/portone/config")
    suspend fun getPortOneConfig(): PortOneConfigDto

    @POST("/api/players/v1/verify-identity")
    suspend fun verifyIdentity(
        @Body request: HashMap<String, String>
    ): Int

    @POST("/api/players/v1/search") //플레이어 검색하기
    suspend fun postPlayersSearch(
        @Body request: PlayerSearchRequest
    ): PlayerSearchResponse

    @POST("/api/deliverys/v1/search/player")//동행내역 검색
    suspend fun postPlayerDeliveryList(
        @Body request: PlayerDeliveryHistoryReq
    ): DeliveryHistoryResponseDto

    @GET("/api/players/v1/calender/{year}/{month}") //캘린더 월정보
    suspend fun getMonthInfo(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): DeliveryCalendarResponseDto

    @GET("/api/players/v1/calender/{year}/{month}/{day}") //캘린더 일정보
    suspend fun getDayInfo(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int
    ): DeliveryCalendarDayResponseDto

}
