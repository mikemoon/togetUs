package sky.kr.co.newtogetusa.data.remote.dto.chat

data class ChatUnreadCountDto(
    val requester_unread_cnt: Int = 0,
    val player_unread_cnt: Int = 0
) {
    val totalCount: Int
        get() = (requester_unread_cnt + player_unread_cnt).coerceAtLeast(0)
}
