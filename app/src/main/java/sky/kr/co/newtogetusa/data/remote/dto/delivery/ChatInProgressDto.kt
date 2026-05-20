package sky.kr.co.newtogetusa.data.remote.dto.delivery

import com.google.gson.annotations.SerializedName

data class ChatInProgressDto(
    @SerializedName("player_id")
    val playerId: Long = 0,
    @SerializedName("room_id")
    val roomId: Long = 0,
    @SerializedName("suggest_yn")
    val suggestYn: String? = null,
    @SerializedName("apply_yn")
    val applyYn: String? = null,
    @SerializedName("player_profile")
    val playerProfile: ChatInProgressPlayerProfileDto? = null,
    @SerializedName("last_msg")
    val lastMsg: ChatInProgressLastMessageDto? = null,
)

data class ChatInProgressPlayerProfileDto(
    @SerializedName("player_id")
    val playerId: Long = 0,
    @SerializedName("user_id")
    val userId: Long = 0,
    val nickname: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    val enable: Boolean = false,
)

data class ChatInProgressLastMessageDto(
    @SerializedName("room_id")
    val roomId: Long = 0,
    @SerializedName("unread_cnt")
    val unreadCnt: Int = 0,
    @SerializedName("msg_id")
    val msgId: String? = null,
    val mimetype: String? = null,
    val msg: String? = null,
    @SerializedName("send_date")
    val sendDate: String? = null,
)
