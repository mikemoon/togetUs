package sky.kr.co.newtogetusa.data.remote.request.chat

data class ChatRoomSearchRequest(
    val type: String,
    val page_no: Int,
    val page_size: Int
)
