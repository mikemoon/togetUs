package sky.kr.co.newtogetusa.ui.main.chat.data

data class ChatListItem(
    val roomId: Int,
    val name:String,
    val date: String,
    val profileUrl: String,
    val unReadCount:Int,
    val message: String = "안녕하세요."
)
