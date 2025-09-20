package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.users.req.ProfileImageRequest

interface PlayerService {

    @POST("/api/players/v1/{player_id}/profile_image")//프로필사진 등록
    suspend fun putProfileImage(
        @Path("player_id") playerId: Int,
        @Body request: ProfileImageRequest
    ):Boolean

    @POST("/api/players/v1/{player_id}/introduction")//자기소개등록
    suspend fun postIntroduction(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
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
    ): Boolean

    @POST("/api/players/v1")//플레이어 신청 등록
    suspend fun postPlayer(
        @Body request: HashMap<String, String>
    ):Boolean

    @POST("/api/players/v1/{player_id}/profile_image")
    suspend fun postPlayerProfileImage(
        @Path("player_id") playerId: Int,
        @Body request: ProfileImageRequest
    ):Boolean

    @POST("/api/players/v1/{player_id}/introduction")
    suspend fun postPlayerIntroduction(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @POST("/api/players/v1/{player_id}/bank")
    suspend fun postPlayerBank(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    )

    @POST("/api/players/v1/{player_id}/area")
    suspend fun postPlayerArea(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @POST("/api/players/v1/{player_id}/apply") //등록심사 요청
    suspend fun postPlayerApply(
        @Path("player_id") playerId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

}