package sky.kr.co.newtogetusa.chat

interface MqttConnectionConfig {
    val brokerUrl: String
    val clientId: String
    val username: String
    val chatTopic: String
}