package sky.kr.co.newtogetusa.data.remote.dto.users

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileDto(
    val user: User,
    val evaluation: Evaluation,
    val requst_count: Int,
    val review_count: Int
): Parcelable {
    @Parcelize
    data class User(
        val user_id: Int,
        val nickname: String,
        val profile_image: String?,
        val enable: Boolean
    ):Parcelable

    @Parcelize
    data class Evaluation(
        val start_average: Int,
        val cancel_count: Int
    ):Parcelable
}