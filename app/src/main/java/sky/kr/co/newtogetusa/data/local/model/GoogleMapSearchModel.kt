package sky.kr.co.newtogetusa.data.local.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GoogleMapSearchModel(
    val name : String,
    val lat : Double?,
    val lng : Double?,
):Parcelable