package sky.kr.co.newtogetusa.chat

interface MqttConnectionConfig {
    val brokerUrl: String
    val clientId: String
    val userNumber: Long
    val qos: Int
    val subscribeTopic: String

    fun publishTopic(category: MqttChatCategory): String
}
