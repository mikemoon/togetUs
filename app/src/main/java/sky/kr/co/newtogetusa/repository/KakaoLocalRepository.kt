package sky.kr.co.newtogetusa.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoSearchAddressResponse

interface KakaoLocalRepository {
    /** @return 도로명 주소 우선, 없으면 지번 주소. 없으면 null */
    suspend fun getAddressFromCoord(lat: Double, lng: Double): KakaoSearchModel?

    suspend fun search(query: String, page: Int, size: Int): KakaoSearchAddressResponse

    fun searchAddressPagingFlow(query: String,
                                analyzeType: String?,
                                pageSize: Int = 30): Flow<PagingData<KakaoSearchModel>>

    fun searchKeywordPagingFlow(
        query: String,
        centerLat: Double? = null,
        centerLng: Double? = null,
        radius: Int? = null,               // 0~20000
        sort: String = "accuracy",         // distance 사용 시 x,y 필요
        pageSize: Int = 15
    ): Flow<PagingData<KakaoSearchModel>>
}