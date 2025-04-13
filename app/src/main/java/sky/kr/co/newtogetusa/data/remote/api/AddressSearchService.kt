package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query
import sky.kr.co.newtogetusa.data.remote.dto.AddressSearchResponse

interface AddressSearchService {
    @Headers("Authorization: KakaoAK 5ce32dc200f3f071017e7a921ae50912")

    @GET("v2/local/search/address.json")
    suspend fun search(
        @Query("query") query: String,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): AddressSearchResponse
}