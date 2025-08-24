package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoCoord2AddressResponse
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoKeywordResponse
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoSearchAddressResponse

interface KakaoLocalService {
    // x=경도(lng), y=위도(lat)
    @GET("v2/local/geo/coord2address.json")
    suspend fun coord2address(
        @Query("x") x: Double,
        @Query("y") y: Double,
        @Query("input_coord") inputCoord: String = "WGS84"
    ): KakaoCoord2AddressResponse

    // 주소 검색 (주소 → 좌표/주소정보)
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Query("query") query: String,
        @Query("analyze_type") analyzeType: String? = null, // "similar" | "exact"
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): KakaoSearchAddressResponse

    // (선택) 키워드로 장소 검색
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("x") x: Double? = null,
        @Query("y") y: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null // "accuracy"|"distance"
    ): KakaoKeywordResponse
}