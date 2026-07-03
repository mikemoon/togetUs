package sky.kr.co.newtogetusa.ui.main.chat

object ChatRoomForegroundTracker {
    @Volatile
    var activeRoomId: Long = -1L
        private set

    fun enter(roomId: Long) {
        activeRoomId = roomId
    }

    fun exit(roomId: Long) {
        if (activeRoomId == roomId) {
            activeRoomId = -1L
        }
    }
}
