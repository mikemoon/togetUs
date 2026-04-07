package sky.kr.co.newtogetusa.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.api.KakaoLocalService
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoSearchAddressResponse
import javax.inject.Inject

class KakaoLocalRepositoryImpl @Inject constructor(
    private val service: KakaoLocalService
) : KakaoLocalRepository {
    override suspend fun getAddressFromCoord(lat: Double, lng: Double): KakaoSearchModel {
        // coord2address는 x=lng, y=lat
        val res = service.coord2address(x = lng, y = lat)
        val doc = res.documents.firstOrNull()
        //doc?.roadAddress?.addressName ?: doc?.address?.addressName
        return KakaoSearchModel(name = doc?.address?.addressName.orEmpty(),
            roadAddress = doc?.roadAddress?.addressName.orEmpty(),
            lat = lat,
            lng = lng,
            subtitle = "",
            distance = null,
            source = "ADDRESS")
    }

    override suspend fun search(query: String, page: Int, size: Int): KakaoSearchAddressResponse =
        service.searchAddress(query = query, page = page, size = size)

    override fun searchAddressPagingFlow(
        query: String,
        analyzeType: String?,
        pageSize: Int
    ): Flow<PagingData<KakaoSearchModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,          // 30 권장
                initialLoadSize = pageSize,   // 첫 로드 사이즈
                enablePlaceholders = false,
                prefetchDistance = pageSize / 2
            ),
            pagingSourceFactory = {
                KakaoSearchAddressPagingSource(
                    service = service,
                    query = query,
                    analyzeType = analyzeType,
                    pageSize = pageSize
                )
            }
        ).flow
    }

    override fun searchKeywordPagingFlow(
        query: String,
        centerLat: Double?,
        centerLng: Double?,
        radius: Int?,               // 0~20000
        sort: String,         // distance 사용 시 x,y 필요
        pageSize: Int
    ): Flow<PagingData<KakaoSearchModel>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = {
                KakaoKeywordAddressPagingSource(service, query, centerLng, centerLat, radius, sort)
            }
        ).flow
}