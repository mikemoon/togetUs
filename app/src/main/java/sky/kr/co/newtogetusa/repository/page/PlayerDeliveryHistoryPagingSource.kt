package sky.kr.co.newtogetusa.repository.page

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.player.DeliverySummaryDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerDeliveryHistoryReq
import sky.kr.co.newtogetusa.repository.PlayerRepository

class PlayerDeliveryHistoryPagingSource(
    private val playerRepository: PlayerRepository,
    private val type: String,
    private val title: String,
    private val pageSize: Int
) : PagingSource<Int, DeliverySummaryDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DeliverySummaryDto> {
        return try {
            val page = params.key ?: 0
            when (
                val response = playerRepository.postPlayerDeliveryList(
                    PlayerDeliveryHistoryReq(
                        type = type,
                        title = title,
                        pageNo = page,
                        pageSize = pageSize
                    )
                )
            ) {
                is ResultWrapper.Success -> {
                    val playerHistoryItems = response.data.deliveries.filter { delivery ->
                        delivery.playerId != null || !delivery.applyDate.isNullOrBlank()
                    }

                    LoadResult.Page(
                        data = playerHistoryItems,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (response.data.hasMore) page + 1 else null
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

    override fun getRefreshKey(state: PagingState<Int, DeliverySummaryDto>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
