package sky.kr.co.newtogetusa.ui.main.chat

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatRoomListUpdate(
    val roomId: Long,
    val message: String? = null,
    val date: String? = null
)

object ChatRoomListUpdateBus {
    private val _updates = MutableSharedFlow<ChatRoomListUpdate>(extraBufferCapacity = 64)
    val updates = _updates.asSharedFlow()

    fun notifyRoomUpdated(roomId: Long) {
        if (roomId <= 0L) return
        _updates.tryEmit(ChatRoomListUpdate(roomId = roomId))
    }

    fun notifyAllRoomsUpdated() {
        _updates.tryEmit(ChatRoomListUpdate(roomId = 0L))
    }

    fun notifyMessageReceived(
        roomId: Long,
        message: String,
        mimeType: String,
        sendDate: String?
    ) {
        if (roomId <= 0L) return
        _updates.tryEmit(
            ChatRoomListUpdate(
                roomId = roomId,
                message = message.toPreviewMessage(mimeType),
                date = sendDate.toChatListDate()
            )
        )
    }

    private fun String.toPreviewMessage(mimeType: String): String =
        when {
            mimeType.startsWith("image/") || isAttachUrl() -> "이미지를 보냈습니다."
            mimeType.startsWith("video/") -> "영상을 보냈습니다."
            else -> this
        }

    private fun String.isAttachUrl(): Boolean =
        (startsWith("http://") || startsWith("https://")) && contains("/attach/")

    private fun String?.toChatListDate(): String {
        val time = toTimestamp() ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            this.time = Date(time)
        }
        val amPm = if (calendar.get(Calendar.HOUR_OF_DAY) < 12) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = calendar.get(Calendar.MINUTE)
        return "• $amPm $hour:${"%02d".format(minute)}"
    }

    private fun String?.toTimestamp(): Long? {
        if (isNullOrBlank()) return null
        val patterns = listOf(
            "yyyy-MM-dd HH:mm:ss.S",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )
        patterns.forEach { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).parse(this)?.time
            }.getOrNull()?.let { return it }
        }
        return null
    }
}
