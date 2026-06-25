package sky.kr.co.newtogetusa.data.remote.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatAttachUploadResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatMessageResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.Room
import sky.kr.co.newtogetusa.data.remote.request.chat.ChatRoomSearchRequest

interface ChatService {

    @GET("/chats/v1/{user_id}/rooms")
    suspend fun getRooms(
        @Path("user_id") userId: Int
    ): List<Room>

    @GET("/chats/v1/{user_id}/rooms/{room_id}/msgs")
    suspend fun getRoomAllMessages(
        @Path("user_id") userId: Int,
        @Path("room_id") roomId: Int
    )

    @GET("/api/chats/v1/rooms/{room_id}/msgs")
    suspend fun getRoomMessages(
        @Path("room_id") roomId: Long,
        @Query("since_id") sinceId: Long = 1,
        @Query("count") count: Int = 100
    ): ChatMessageResponseDto

    @GET("/api/chats/v1/rooms")
    suspend fun getChatRooms(): List<ChatRoomDto>

    @GET("/api/chats/v1/rooms/{room_id}")
    suspend fun getChatRoom(
        @Path("room_id") roomId: Long
    ): ChatRoomDto

    @POST("api/chats/v1/search/player")
    suspend fun searchPlayerRooms(
        @Body body : ChatRoomSearchRequest
    ): ChatRoomSearchResponseDto

    @POST("api/chats/v1/search/requester")
    suspend fun searchUserRooms(
        @Body body : ChatRoomSearchRequest
    ): ChatRoomSearchResponseDto

    @POST("api/chats/v1/rooms/{room_id}/setting/noti_y")
    suspend fun chatRoomNotiOn(
        @Path("room_id") roomId: Long
    ): Boolean

    @POST("api/chats/v1/rooms/{room_id}/setting/noti_n")
    suspend fun chatRoomNotiOff(
        @Path("room_id") roomId: Long
    ): Boolean

    @POST("api/chats/v1/rooms/{room_id}/setting/report")
    suspend fun chatRoomReport(
        @Path("room_id") roomId: Long
    ): Boolean

    @POST("api/chats/v1/rooms/{room_id}/setting/block")
    suspend fun chatRoomBlock(
        @Path("room_id") roomId: Long
    ): Boolean

    @POST("api/chats/v1/rooms/{room_id}/setting/unblock")
    suspend fun chatRoomUnblock(
        @Path("room_id") roomId: Long
    ): Boolean

    @POST("api/chats/v1/rooms/{room_id}/setting/exit")
    suspend fun chatRoomExit(
        @Path("room_id") roomId: Long
    ): Boolean

    @Multipart
    @POST("api/chats/v1/rooms/{room_id}/attach")
    suspend fun uploadChatAttach(
        @Path("room_id") roomId: Long,
        @Query("msg_ptr_id") messagePointerId: Long? = null,
        @Part file: MultipartBody.Part
    ): ChatAttachUploadResponseDto

}
