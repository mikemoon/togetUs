package sky.kr.co.newtogetusa.chat

interface ChatClient {
    fun connect()
    fun disconnect()
    fun sendMessage(message: String)
    fun publish(category: MqttChatCategory, payload: String)
    fun isConnected(): Boolean
}
