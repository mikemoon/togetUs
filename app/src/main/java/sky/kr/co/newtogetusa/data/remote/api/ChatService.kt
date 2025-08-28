package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.chat.Room

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
}