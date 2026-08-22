package sky.kr.co.newtogetusa.data.remote.dto.delivery

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ReviewCheckDto(
    @SerializedName("can_review")
    val canReview: Boolean = false,
    val message: String? = null,
)

@Parcelize
data class DeliveryReviewDto(
    @SerializedName("review_id")
    val reviewId: Long = 0,
    val stars: Int = 0,
    val contents: String? = null,
    val items: List<String> = emptyList(),
    @SerializedName("reg_date")
    val regDate: String? = null,
    @SerializedName("user_id")
    val userId: Long = 0,
    val nickname: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
) : Parcelable
