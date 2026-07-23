package sky.kr.co.newtogetusa.data.remote.dto.chat

import com.google.gson.annotations.SerializedName

data class ChatRoomDto(
    val room_id: Int,
    val room_name: String,
    val read_msg_id: Int,
    val partner_read_msg_id: Int = 0,
    val unread_cnt: Int,
    val delivery: ChatRoomDeliveryDto?,
    val last_msg: ChatRoomLastMessageDto?,
    @SerializedName("noti_yn")
    val noti_yn: String? = null,
    @SerializedName("blocked_yn")
    val blocked_yn: String? = null,
    @SerializedName("is_noti_on")
    val is_noti_on: Boolean? = null,
    @SerializedName("is_cancel")
    val is_cancel: Boolean = false,
    val is_reported: Boolean = false,
    @SerializedName("exit_yn")
    val exit_yn: String? = null,
) {
    // 채팅방 상세 API의 실제 상태값은 noti_yn(Y/N)이며, 이전 응답의 Boolean 필드도 호환한다.
    val isNotificationOn: Boolean
        get() = when (noti_yn?.uppercase()) {
            "Y" -> true
            "N" -> false
            else -> is_noti_on ?: true
        }

    val is_blocked: Boolean
        get() = blocked_yn?.uppercase() == "Y"

    val is_exited: Boolean
        get() = exit_yn?.uppercase() == "Y"

    val is_chat_disabled: Boolean
        get() = is_blocked || is_exited || is_cancel || is_reported ||
            delivery?.status_cd in DISABLED_DELIVERY_STATUSES

    val chat_disabled_message: String
        get() = when {
            is_blocked -> "차단되어 메시지를 볼수 없습니다."
            is_exited -> "나간 채팅방에서는 메시지를 보낼 수 없습니다."
            is_cancel || is_reported -> "상대방과 대화가 불가능합니다."
            else -> "거래가 종료되어 메시지를 보낼 수 없습니다."
        }

    private companion object {
        val DISABLED_DELIVERY_STATUSES = setOf("DELIVERY_END", "DONE", "DONE_END", "CANCEL")
    }
}

data class ChatRoomDeliveryDto(
    val delivery_id: Int,
    val status_cd: String,
    val complete_date: String?,
    val suggest_yn: String?,
    val apply_yn: String?,
    val prd_picture: String? = null
)

data class ChatRoomLastMessageDto(
    val room_id: Int?,
    val msg_id: Int,
    val msg_ptr_id: Int,
    val mimetype: String,
    val msg: String,
    val send_uid: Int,
    val send_uname: String,
    val send_date: String,
    val display_send_date: String
)
