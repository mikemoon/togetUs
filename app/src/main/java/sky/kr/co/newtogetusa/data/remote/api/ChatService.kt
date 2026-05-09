package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    @GET("/api/chats/v1/rooms")
    suspend fun getChatRooms(): List<ChatRoomDto>

    @POST("api/chats/v1/search/player")
    suspend fun searchPlayerRooms(
        @Body body : ChatRoomSearchRequest
    ): ChatRoomSearchResponseDto

    @POST("api/chats/v1/search/requester")
    suspend fun searchUserRooms(
        @Body body : ChatRoomSearchRequest
    ): ChatRoomSearchResponseDto

}
