package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import sky.kr.co.newtogetusa.data.remote.dto.DirectionsResponse
import sky.kr.co.newtogetusa.data.remote.dto.PlaceDetailsResponse

interface DirectionsApiService {

    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String?,
        @Query("mode") mode: String = "transit",
        @Query("key") apiKey: String
    ): DirectionsResponse

    @GET("maps/api/directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String? = null,
        @Query("mode") mode: String = "driving",
        @Query("key") apiKey: String,
        @Query("region") region: String = "kr"
    ): DirectionsResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "geometry",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse

}