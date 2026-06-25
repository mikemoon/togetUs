package sky.kr.co.newtogetusa.data.remote.dto.chat

data class ChatAttachUploadResponseDto(
    val url: String = "",
    val room_id: Long = 0,
    val msg_id: Long = 0,
    val msg_ptr_id: Long = 0,
    val send_uid: Long = 0,
    val send_uname: String = "",
    val send_date: String = "",
    val mimetype: String = ""
)
