package sky.kr.co.newtogetusa.repository.page

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.repository.DeliveryRepository

class DeliveryLikePagingSource(
    private val repo: DeliveryRepository,
    private val type: String,
    private val title: String
) : PagingSource<Int, DeliverySummaryDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DeliverySummaryDto> {
        return try {
            val page = params.key ?: 0
            val response = repo.postPlayerDeliveryLikeSearch(
                DeliverySearchReq(
                    type = type,
                    title = title,
                    page_no = page,
                    page_size = params.loadSize
                )
            )

            if (response is ResultWrapper.Success) {
                LoadResult.Page(
                    data = response.data.deliveries.map { it.apply { setUiValue() } },
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (response.data.has_more) page + 1 else null
                )
            } else {
                LoadResult.Error(Exception("API Error"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DeliverySummaryDto>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}
