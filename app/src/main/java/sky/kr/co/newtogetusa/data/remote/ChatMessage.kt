package sky.kr.co.newtogetusa.data.remote

data class ChatMessage(
    val id: Long,
    val messagePointerId: Long = 0,
    val sender: String,
    val content: String,
    val messageType: Int,
    val messageImageUrl:String? = null,
    val messageVieoUrl:String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMyMessage: Boolean = false,
    val isUnread: Boolean = false
)
