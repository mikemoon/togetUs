package sky.kr.co.newtogetusa.ui.main.delivery

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResultModel(
    val placeId: String?,
    val distance: String? = null,
    val placeName: String?,
    val detailAddress: String? = null
): Parcelable
