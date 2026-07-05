package sky.kr.co.newtogetusa.data.remote.dto.chat

data class ChatRoomDto(
    val room_id: Int,
    val room_name: String,
    val read_msg_id: Int,
    val partner_read_msg_id: Int = 0,
    val unread_cnt: Int,
    val delivery: ChatRoomDeliveryDto?,
    val last_msg: ChatRoomLastMessageDto?,
    val is_blocked: Boolean = false,
    val is_noti_on: Boolean = true,
    val is_reported: Boolean = false
)

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
