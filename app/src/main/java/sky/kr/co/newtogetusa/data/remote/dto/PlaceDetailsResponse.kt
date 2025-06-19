package sky.kr.co.newtogetusa.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlaceDetailsResponse(
    @SerializedName("result") val result: PlaceResult?
)

data class PlaceResult(
    @SerializedName("geometry") val geometry: Geometry?
)

data class Geometry(
    @SerializedName("location") val location: LatLngLocation?
)

data class LatLngLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)
