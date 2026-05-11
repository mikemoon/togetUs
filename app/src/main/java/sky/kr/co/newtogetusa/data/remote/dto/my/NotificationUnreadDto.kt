package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class NotificationUnreadDto(
    @field:SerializedName("unread_cnt") val unreadCount: Int
)
