package sky.kr.co.newtogetusa.repository.page

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.users.DeliveryItem
import sky.kr.co.newtogetusa.data.remote.request.user.DeliveryListSearchRequest
import sky.kr.co.newtogetusa.repository.UserRepository

class DeliveryRequestSearchPagingSource(
    private val userRepository: UserRepository,
    private val myArea: Boolean,
    private val departCd: List<String>,
    private val destCd: List<String>,
    private val sortType: String,
    private val fee: Int,
    private val face2Face: Boolean?,
    private val immediately: String?,
    private val pageSize: Int
) : PagingSource<Int, DeliveryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DeliveryItem> {
        return try {
            val page = params.key ?: 0
            when (
                val response = userRepository.postUsersSearch(
                    DeliveryListSearchRequest(
                        myarea = myArea,
                        depart_cd = departCd,
                        dest_cd = destCd,
                        sort_type = sortType,
                        fee = fee,
                        face2face = face2Face,
                        immediately = immediately,
                        page_no = page,
                        page_size = pageSize
                    )
                )
            ) {
                is ResultWrapper.Success -> {
                    LoadResult.Page(
                        data = response.data.deliveries,
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

    override fun getRefreshKey(state: PagingState<Int, DeliveryItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}