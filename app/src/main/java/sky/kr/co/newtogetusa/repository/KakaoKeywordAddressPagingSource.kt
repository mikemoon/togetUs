package sky.kr.co.newtogetusa.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.api.KakaoLocalService
import kotlin.math.min

class KakaoKeywordAddressPagingSource(
    private val service: KakaoLocalService,
    private val query: String,
    private val centerLng: Double?,   // x (경도) - distance sort/반경 시 필요
    private val centerLat: Double?,   // y (위도)
    private val radius: Int?,         // 0~20000
    private val sort: String          // "accuracy" | "distance"
) : PagingSource<Int, KakaoSearchModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, KakaoSearchModel> =
        try {
            val page = params.key ?: 1
            val res = service.searchKeyword(
                query = query,
                page = page,
                size = min(params.loadSize, 15),
                sort = sort,
                x = centerLng,
                y = centerLat,
                radius = radius
            )
            val items = res.documents.map { p ->
                KakaoSearchModel(
                    name = p.address_name.orEmpty(),
                    lat = p.y?.toDoubleOrNull(),
                    lng = p.x?.toDoubleOrNull(),
                    subtitle = p.place_name,
                    roadAddress = p.road_address_name,
                    source = "KEYWORD",
                )
            }
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (res.meta.is_end) null else page + 1
            )
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }

    override fun getRefreshKey(state: PagingState<Int, KakaoSearchModel>): Int? =
        state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
}