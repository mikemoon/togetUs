package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoNaviDirectionsResponse

interface KakaoNaviService {
    // origin/destination은 "x,y" = "경도,위도" 문자열
    @GET("v1/directions")
    suspend fun directions(
        @Query("origin") origin: String,            // "127.0,37.5"  (x=lng, y=lat)
        @Query("destination") destination: String,  // "127.1,37.4"
        @Query("priority") priority: String? = "RECOMMEND",
        @Query("alternatives") alternatives: Boolean? = false,
        @Query("summary") summary: Boolean? = false,
        @Query("road_details") roadDetails: Boolean? = false,
        @Query("waypoints") waypoints: String? = null // "x1,y1|x2,y2" 최대 5개
    ): KakaoNaviDirectionsResponse
}