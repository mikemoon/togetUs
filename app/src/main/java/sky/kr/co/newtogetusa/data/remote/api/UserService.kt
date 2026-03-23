package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.users.BannerDto
import sky.kr.co.newtogetusa.data.remote.dto.users.BannerLandingDto
import sky.kr.co.newtogetusa.data.remote.dto.users.DeliverySearchListResponse
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.data.remote.request.user.DeliveryListSearchRequest
import sky.kr.co.newtogetusa.data.remote.request.user.ProfileImageRequest

interface UserService {

    @GET("/api/users/v1/me/profile")
    suspend fun getMyProfile(
    ): ProfileDto

    @GET("/api/users/v1/{user_id}/profile")
    suspend fun getProfile(
        @Path("user_id") userId: Int
    ): ProfileDto

    @POST("/api/users/v1/{user_id}/nickname")
    suspend fun putProfileNickname(
        @Path("user_id") userId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @POST("/api/users/v1/{user_id}/profile_image")
    suspend fun putProfileImage(
        @Path("user_id") userId: Int,
        @Body request: ProfileImageRequest
    ):Boolean

    @POST("/api/users/v1/{user_id}/report")//신고하기
    suspend fun postReportUser(
        @Path("user_id") userId: Int,
        @Body request: HashMap<String, String>
    ): PlayerInfoDto

    @POST("/api/users/v1/{user_id}/block")//차단하기
    suspend fun postBlockUser(
        @Path("user_id") userId: Int,
        @Body request: HashMap<String, String>
    ): PlayerInfoDto

    @GET("/api/users/v1/{user_id}/reviews")//리뷰보기
    suspend fun getReviews(
        @Path("user_id") userId: Int
    ):PlayerInfoDto

    @POST("/api/users/v1/{user_id}/reviews")//리뷰등록
    suspend fun postReview(
        @Path("user_id") userId: Int,
        @Body request: HashMap<String, String>
    ):Boolean

    @GET("/api/users/v1/banner")
    suspend fun getBanners():List<BannerDto>

    @GET("/api/users/v1/banner/{bn_id}")
    suspend fun getBannerDetail(
        @Path("bn_id") bnId: Int,
    ): BannerLandingDto

    @POST("/api/users/v1/search") //플레이어 동행요청 검색
    suspend fun postUsersSearch(
        @Body request: DeliveryListSearchRequest
    ): DeliverySearchListResponse


}