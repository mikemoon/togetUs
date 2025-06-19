package sky.kr.co.newtogetusa.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import sky.kr.co.newtogetusa.data.local.model.RoutePolylineDecoder
import sky.kr.co.newtogetusa.data.remote.api.DirectionsApiService
import sky.kr.co.newtogetusa.data.remote.dto.DirectionsResponse
import timber.log.Timber
import javax.inject.Inject

class DirectionsRepository @Inject constructor(
    private val apiService: DirectionsApiService,
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
}