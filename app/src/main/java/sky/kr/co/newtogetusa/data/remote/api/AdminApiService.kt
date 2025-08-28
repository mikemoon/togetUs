package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

interface AdminApiService {
    @GET("/admin/users/{user_id}")
    suspend fun getUserDetail(
        @Query("user_id") userId:Int
    ): Boolean

    @GET("/admin/users/{user_id}/logs")
    suspend fun getUserLogs(
        @Query("user_id") userId:Int
    ):Boolean

    @GET("/admin/users")
    suspend fun getUsers(): Boolean

}