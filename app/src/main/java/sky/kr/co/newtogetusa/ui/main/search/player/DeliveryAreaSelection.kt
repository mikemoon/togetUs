package sky.kr.co.newtogetusa.ui.main.search.player

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeliveryAreaSelection(
    val name: String,
    val radiusKm: Int,
    val lat: Double?,
    val lng: Double?
) : Parcelable