package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.users.req.ProfileImageRequest

interface UserService {

    @GET("/api/users/v1/me/profile")
    suspend fun getMyProfile(
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

}