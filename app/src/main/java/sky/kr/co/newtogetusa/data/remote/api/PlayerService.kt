package sky.kr.co.newtogetusa.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerApplyedInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerSearchResponse
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.users.req.ProfileImageRequest
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
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

    @POST("/api/players/v1/{player_id}/req_delivery") //배송요청 제안
    suspend fun postReqDelivery(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ): PlayerInfoDto

    @GET("/api/players/v1/{player_id}/reviews")
    suspend fun getReviews(
        @Path("player_id") playerId: Int
    ):PlayerInfoDto

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
    ):Boolean

    @POST("/api/players/v1/{player_id}/apply") //등록심사 요청
    suspend fun postPlayerApply(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @Multipart
    @POST("/api/players/v1/{player_id}/apply_batch") //등록심사 일괄요청
    suspend fun postPlayerApplyBatch(
        @Path("player_id") playerId: Int,
        @Part("data") data: RequestBody,
        @Part profile_image: MultipartBody.Part?,
        @Part criminal_record: MultipartBody.Part?
    )

    @POST("/api/players/v1/verify-imp-uid") //본인인증
    suspend fun verifyImpUid(
        @Body request: HashMap<String, String>
    ): Int

    @POST("/api/players/v1/search") //플레이어검색
    suspend fun postPlayersSearch(
        @Body request: PlayerSearchRequest
    ): PlayerSearchResponse


}