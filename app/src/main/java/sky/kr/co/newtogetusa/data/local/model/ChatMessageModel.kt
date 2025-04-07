package sky.kr.co.newtogetusa.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_chat_message")
class ChatMessageModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val content: String,
    val messageType: Int,
    val messageImageUrl:String? = null,
    val messageVieoUrl:String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMyMessage: Boolean = false
)