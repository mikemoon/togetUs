package sky.kr.co.newtogetusa.repository

import android.net.http.HttpException
import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.api.KakaoLocalService
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoSearchAddressResponse
import java.io.IOException
import kotlin.math.min

class KakaoSearchAddressPagingSource(
    private val service: KakaoLocalService,
    private val query: String,
    private val analyzeType: String? = null,
    private val pageSize: Int = 30 // Kakao 최대 30
) : PagingSource<Int, KakaoSearchModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, KakaoSearchModel> =
        try {
            val page = params.key ?: 1
            val res = service.searchAddress(query = query, page = page, size = min(params.loadSize, 30), analyzeType = analyzeType)
            val items = res.documents.map { doc ->
                val name = doc.roadAddress?.addressName ?: doc.address?.addressName ?: doc.addressName.orEmpty()
                KakaoSearchModel(
                    name = name,
                    lat = doc.y?.toDoubleOrNull(),
                    lng = doc.x?.toDoubleOrNull(),
                    subtitle = null,
                    roadAddress = doc.roadAddress?.roadName,
                    source = "ADDRESS"
                )
            }
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (res.meta.isEnd) null else page + 1
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