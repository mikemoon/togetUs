package sky.kr.co.newtogetusa.chat

interface ChatClient {
    fun setSession(userNumber: Long, accessToken: String? = null)
    fun connect()
    fun disconnect()
    fun sendMessage(message: String)
    fun sendMessage(roomId: Long, message: String, mimeType: String = "text/plain", messagePointerId: Long = 0)
    fun sendAttach(roomId: Long, mimeType: String, url: String, messagePointerId: Long = 0)
    fun sendRead(roomId: Long, messageId: Long)
    fun sendGps(roomId: Long, latitude: Double, longitude: Double)
    fun publish(category: MqttChatCategory, payload: String)
    fun isConnected(): Boolean
}
