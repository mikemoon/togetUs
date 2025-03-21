package sky.kr.co.newtogetusa.data.remote

data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMyMessage: Boolean = false
)
