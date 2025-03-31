package sky.kr.co.newtogetusa.data.remote

data class ChatMessage(
    val sender: String,
    val content: String,
    val messageType: Int,
    val messageImageUrl:String? = null,
    val messageVieoUrl:String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMyMessage: Boolean = false
)
