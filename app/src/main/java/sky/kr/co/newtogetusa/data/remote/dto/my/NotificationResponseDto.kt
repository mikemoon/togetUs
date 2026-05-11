package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class NotificationResponseDto(
    @field:SerializedName("page_no") val pageNo: Int,
    @field:SerializedName("total_cnt") val totalCount: Int,
    @field:SerializedName("has_more") val hasMore: Boolean,
    @field:SerializedName("unread_cnt") val unreadCount: Int,
    @field:SerializedName("notifications") val notifications: List<NotificationDto>
)

data class NotificationDto(
    @field:SerializedName("noti_id") val notificationId: Long,
    @field:SerializedName("user_id") val userId: Long,
    @field:SerializedName("receiver_type") val receiverType: String,
    @field:SerializedName("noti_type") val notificationType: String,
    @field:SerializedName("title") val title: String,
    @field:SerializedName("body") val body: String,
    @field:SerializedName("landing_type") val landingType: String,
    @field:SerializedName("landing_id") val landingId: Long?,
    @field:SerializedName("read_yn") var readYn: String,
    @field:SerializedName("read_date") val readDate: String?,
    @field:SerializedName("reg_date") val regDate: String
) {
    val isUnread: Boolean
        get() = readYn.equals("N", ignoreCase = true)
}
