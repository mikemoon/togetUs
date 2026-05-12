package sky.kr.co.newtogetusa.chat

data class DefaultMqttConnectionConfig(
    override val brokerUrl: String = BROKER_URL,
    override val userNumber: Long = DEFAULT_USER_NUMBER,
    override val qos: Int = DEFAULT_QOS
): MqttConnectionConfig {
    override val clientId: String = "TogetUs-AOS-$userNumber-${System.currentTimeMillis()}"
    override val subscribeTopic: String = "$SUB_PREFIX/$userNumber/#"

    override fun publishTopic(category: MqttChatCategory): String =
        "$PUB_PREFIX/$userNumber/${category.topicName}/"

    companion object {
        const val BROKER_URL = "tcp://www.togetus.net:1883"
        private const val PUB_PREFIX = "togetus-pub"
        private const val SUB_PREFIX = "togetus-sub"
        private const val DEFAULT_QOS = 1
        private const val DEFAULT_USER_NUMBER = 111L
    }
}
