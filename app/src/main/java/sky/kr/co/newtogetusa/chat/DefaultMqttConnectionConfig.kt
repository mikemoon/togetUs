package sky.kr.co.newtogetusa.chat

data class DefaultMqttConnectionConfig(
    override val brokerUrl: String,
    override val username: String,
    override val chatTopic: String
): MqttConnectionConfig {
    override val clientId: String = "KotlinMqttChat-${System.currentTimeMillis()}"
}