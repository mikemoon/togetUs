package sky.kr.co.newtogetusa.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import sky.kr.co.newtogetusa.data.local.model.RoutePolylineDecoder
import sky.kr.co.newtogetusa.data.remote.api.DirectionsApiService
import sky.kr.co.newtogetusa.data.remote.api.KakaoNaviService
import sky.kr.co.newtogetusa.data.remote.dto.DirectionsResponse
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoNaviDirectionsResponse
import timber.log.Timber
import javax.inject.Inject

class DirectionsRepository @Inject constructor(
    private val apiService: DirectionsApiService,
    private val service: KakaoNaviService,
    @ApplicationContext private val context: Context) {

    suspend fun getRoutePoints(
        origin: LatLng,
        destination: LatLng,
        waypoints: List<LatLng> = emptyList(),
        apiKey: String
    ): Result<List<LatLng>> {
        return try {
            val originStr = "${origin.latitude},${origin.longitude}"
            val destinationStr = "${destination.latitude},${destination.longitude}"
            val waypointStr = if (waypoints.isNotEmpty()) {
                waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
            } else null

            val response = apiService.getDirections(originStr, destinationStr, waypointStr, apiKey =  apiKey)

            val points = response.routes.firstOrNull()
                ?.overviewPolyline?.points
                ?.let { RoutePolylineDecoder.decodePolyline(it) }

            if (points != null) Result.success(points)
            else Result.failure(Exception("경로 없음"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRouteByPlaceIds(
        originPlaceId: String,
        destinationPlaceId: String,
        waypointsPlaceIds: List<String>? = null
    ): DirectionsResponse {
        val origin = "place_id:$originPlaceId"
        val destination = "place_id:$destinationPlaceId"
        val waypoints = waypointsPlaceIds?.joinToString("|") { "place_id:$it" }

        return apiService.getRoute(
            origin = origin,
            destination = destination,
            waypoints = waypoints,
            apiKey = "AIzaSyA9ZdMta--H5FgxapDt2AyHV6ZooYBht54"
        )
    }


    suspend fun getLatLngByPlaceId(placeId: String): LatLng? {
        return try {
            val response = apiService.getPlaceDetails(placeId = placeId, apiKey = "AIzaSyA9ZdMta--H5FgxapDt2AyHV6ZooYBht54")
            val location = response.result?.geometry?.location
            if (location != null) LatLng(location.lat, location.lng) else null
        } catch (e: Exception) {
            Timber.e("Error fetching place details: ${e.message}")
            null
        }
    }

    //kakao
    suspend fun fetchRoute(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double,
        waypoints: List<Pair<Double, Double>> = emptyList()
    ): Pair<List<com.kakao.vectormap.LatLng>, KakaoNaviDirectionsResponse.Summary?> {

        // API는 "경도,위도" 순서! (x=lng, y=lat)
        val origin = "${startLng},${startLat}"
        val dest = "${endLng},${endLat}"
        val wp = if (waypoints.isNotEmpty())
            waypoints.joinToString("|") { "${it.second},${it.first}" } // (lat,lng) -> "lng,lat"
        else null

        val res = service.directions(
            origin = origin,
            destination = dest,
            priority = "RECOMMEND",
            alternatives = false,
            summary = false,
            roadDetails = false,
            waypoints = wp
        )

        val route = res.routes.firstOrNull() ?: return emptyList<com.kakao.vectormap.LatLng>() to null

        // roads[].vertexes = [x1,y1,x2,y2,...] -> Kakao LatLng 리스트로 변환 (Lat,Lng 순으로 넣기)
        val points = mutableListOf<com.kakao.vectormap.LatLng>()
        route.sections.forEach { section ->
            section.roads.forEach { road ->
                val v = road.vertexes
                var i = 0
                while (i + 1 < v.size) {
                    val x = v[i]       // 경도
                    val y = v[i + 1]   // 위도
                    points += com.kakao.vectormap.LatLng.from(y, x)
                    i += 2
                }
            }
        }
        return points to route.summary
    }
}