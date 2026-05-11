package sky.kr.co.newtogetusa.chat

enum class MqttChatCategory(val topicName: String) {
    MSG("msg"),
    READ("read"),
    ATTACH("attach"),
    GPS("gps");

    companion object {
        fun fromTopicName(topicName: String): MqttChatCategory? =
            entries.firstOrNull { it.topicName == topicName }
    }
}
