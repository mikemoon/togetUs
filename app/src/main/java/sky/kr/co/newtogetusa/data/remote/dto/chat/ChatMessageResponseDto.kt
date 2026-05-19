package sky.kr.co.newtogetusa.data.remote.dto.chat

data class ChatMessageResponseDto(
    val msgs: List<ChatMessageDto>,
    val has_more: Boolean
)

data class ChatMessageDto(
    val room_id: Long,
    val msg_id: Long,
    val msg_ptr_id: Long,
    val mimetype: String,
    val msg: String,
    val send_uid: Long,
    val send_uname: String?,
    val send_date: String
)
