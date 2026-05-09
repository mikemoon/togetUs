package sky.kr.co.newtogetusa.repository.page

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchRoomDto
import sky.kr.co.newtogetusa.data.remote.request.chat.ChatRoomSearchRequest
import sky.kr.co.newtogetusa.repository.ChatRepository

class ChatRoomPagingSource(
    private val chatRepository: ChatRepository,
    private val type: String,
    private val pageSize: Int,
    private val isPlayerMode: Boolean
) : PagingSource<Int, ChatRoomSearchRoomDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatRoomSearchRoomDto> {
        return try {
            val page = params.key ?: 0
            val request = ChatRoomSearchRequest(
                type = type,
                page_no = page,
                page_size = pageSize
            )

            when (
                val response = if (isPlayerMode) {
                    chatRepository.searchPlayerRooms(request)
                } else {
                    chatRepository.searchUserRooms(request)
                }
            ) {
                is ResultWrapper.Success -> {
                    LoadResult.Page(
                        data = response.data.rooms,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (response.data.has_more) page + 1 else null
                    )
                }

                is ResultWrapper.GenericError -> LoadResult.Error(
                    IllegalStateException(response.message ?: "API Error")
                )

                ResultWrapper.NetworkError -> LoadResult.Error(
                    IllegalStateException("Network Error")
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ChatRoomSearchRoomDto>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
