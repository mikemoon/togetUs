package sky.kr.co.newtogetusa.data.local.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KakaoSearchModel(
    val name : String,
    val lat : Double?,
    val lng : Double?,
    val subtitle: String?,  // 주소, 카테고리 등 보조 텍스트
    val roadAddress: String?,
    val source: String?             // "ADDRESS" | "KEYWORD"
): Parcelable {
}