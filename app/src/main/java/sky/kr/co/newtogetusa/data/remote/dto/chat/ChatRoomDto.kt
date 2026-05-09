package sky.kr.co.newtogetusa.data.remote.dto.chat

data class ChatRoomDto(
    val room_id: Int,
    val room_name: String,
    val read_msg_id: Int,
    val unread_cnt: Int,
    val delivery: ChatRoomDeliveryDto,
    val last_msg: ChatRoomLastMessageDto?
)

data class ChatRoomDeliveryDto(
    val delivery_id: Int,
    val status_cd: String,
    val complete_date: String?,
    val suggest_yn: String?,
    val apply_yn: String?
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
