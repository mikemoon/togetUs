package sky.kr.co.newtogetusa.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.ChatService
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchRoomDto
import sky.kr.co.newtogetusa.data.remote.request.chat.ChatRoomSearchRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import sky.kr.co.newtogetusa.repository.page.ChatRoomPagingSource
import javax.inject.Inject

class ChatRepository @Inject constructor(
    @NetworkModule.ChatApi private val apiService: ChatService
) : BaseNetRepo() {

    suspend fun getChatRooms() = safeApiCall<List<ChatRoomDto>>(Dispatchers.IO){
        apiService.getChatRooms()
    }

    suspend fun getChatRoom(roomId: Long) = safeApiCall<ChatRoomDto>(Dispatchers.IO) {
        apiService.getChatRoom(roomId)
    }

    suspend fun getRoomMessages(roomId: Long, sinceId: Long = 1, count: Int = 100) = safeApiCall(Dispatchers.IO) {
        apiService.getRoomMessages(roomId, sinceId, count)
    }

    suspend fun searchPlayerRooms(request: ChatRoomSearchRequest) = safeApiCall<ChatRoomSearchResponseDto>(Dispatchers.IO){
        apiService.searchPlayerRooms(request)
    }

    suspend fun searchUserRooms(request: ChatRoomSearchRequest) = safeApiCall<ChatRoomSearchResponseDto>(Dispatchers.IO){
        apiService.searchUserRooms(request)
    }

    suspend fun chatRoomNotiOff(roomId: Long) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.chatRoomNotiOff(roomId)
    }

    suspend fun chatRoomNotiOn(roomId: Long) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.chatRoomNotiOn(roomId)
    }

    suspend fun chatRoomReport(roomId: Long) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.chatRoomReport(roomId)
    }

    suspend fun chatRoomBlock(roomId: Long) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.chatRoomBlock(roomId)
    }

    suspend fun chatRoomUnblock(roomId: Long) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.chatRoomUnblock(roomId)
    }

    suspend fun chatRoomExit(roomId: Long) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.chatRoomExit(roomId)
    }

    suspend fun uploadChatAttach(roomId: Long, messagePointerId: Long, file: MultipartBody.Part) = safeApiCall(Dispatchers.IO) {
        apiService.uploadChatAttach(
            roomId = roomId,
            messagePointerId = messagePointerId.takeIf { it > 0 },
            file = file
        )
    }

    fun getPlayerRoomPagingFlow(type: String, pageSize: Int = 10): Flow<PagingData<ChatRoomSearchRoomDto>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChatRoomPagingSource(
                    chatRepository = this,
                    type = type,
                    pageSize = pageSize,
                    isPlayerMode = true
                )
            }
        ).flow

    fun getUserRoomPagingFlow(type: String, pageSize: Int = 10): Flow<PagingData<ChatRoomSearchRoomDto>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChatRoomPagingSource(
                    chatRepository = this,
                    type = type,
                    pageSize = pageSize,
                    isPlayerMode = false
                )
            }
        ).flow
}
