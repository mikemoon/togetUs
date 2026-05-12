package sky.kr.co.newtogetusa.chat

import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import timber.log.Timber
import javax.inject.Inject

class MqttChatClientImpl @Inject constructor(
    private val config: MqttConnectionConfig,
    private val messageHandler: MessageHandler
): ChatClient{
    private val client: MqttClient
    private val connectOptions = MqttConnectOptions()
    private val gson = Gson()
    private val persistence = MemoryPersistence()

    init {
        client = MqttClient(config.brokerUrl, config.clientId, persistence)
        connectOptions.apply {
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 30
            isAutomaticReconnect = true
        }

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                messageHandler.onConnectionLost(cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic == null || message == null) return

                try {
                    val category = topic.toMqttChatCategory()
                    val payload = String(message.payload, Charsets.UTF_8)
                    messageHandler.handleIncomingMessage(category?.topicName ?: topic, payload)
                } catch (e: Exception) {
                    Timber.e(e, "MQTT message handling failed. topic=%s", topic)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                messageHandler.onDeliveryComplete(token)
            }
        })
    }

    override fun connect() {
        try {
            if (!client.isConnected) {
                Timber.d("MQTT connecting. broker=%s clientId=%s", config.brokerUrl, config.clientId)
                client.connect(connectOptions)
            }
            client.subscribe(config.subscribeTopic, config.qos)
            Timber.d("MQTT subscribed. topic=%s", config.subscribeTopic)
        } catch (e: MqttException) {
            Timber.e(e, "MQTT connect failed")
        }
    }

    override fun disconnect() {
        try {
            if (client.isConnected) {
                client.disconnect()
                Timber.d("MQTT disconnected")
            }
        } catch (e: MqttException) {
            Timber.e(e, "MQTT disconnect failed")
        }
    }

    override fun sendMessage(message: String) {
        publish(MqttChatCategory.MSG, message)
    }

    override fun sendMessage(roomId: Long, message: String, mimeType: String, messagePointerId: Long) {
        val payload = MqttMessagePayload(
            roomId = roomId,
            messagePointerId = messagePointerId,
            mimeType = mimeType,
            message = message
        )
        publish(MqttChatCategory.MSG, gson.toJson(payload))
    }

    override fun sendAttach(roomId: Long, mimeType: String, base64: String, messagePointerId: Long) {
        val payload = MqttMessagePayload(
            roomId = roomId,
            messagePointerId = messagePointerId,
            mimeType = mimeType,
            message = base64
        )
        publish(MqttChatCategory.ATTACH, gson.toJson(payload))
    }

    override fun sendRead(roomId: Long, messageId: Long) {
        publish(
            MqttChatCategory.READ,
            gson.toJson(MqttReadPayload(roomId = roomId, messageId = messageId))
        )
    }

    override fun sendGps(roomId: Long, latitude: Double, longitude: Double) {
        publish(
            MqttChatCategory.GPS,
            gson.toJson(MqttGpsPayload(roomId = roomId, latitude = latitude, longitude = longitude))
        )
    }

    override fun publish(category: MqttChatCategory, payload: String) {
        try {
            if (!client.isConnected) {
                connect()
            }

            val mqttMessage = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
                qos = config.qos
                isRetained = false
            }
            val topic = config.publishTopic(category)
            client.publish(topic, mqttMessage)
            Timber.d("MQTT published. topic=%s", topic)
        } catch (e: MqttException) {
            Timber.e(e, "MQTT publish failed. category=%s", category.topicName)
        }
    }

    override fun isConnected(): Boolean {
        return client.isConnected
    }

    private fun String.toMqttChatCategory(): MqttChatCategory? {
        val parts = split("/")
        return if (parts.size >= 3 && parts[0] == "togetus-sub" && parts[1] == config.userNumber.toString()) {
            parts.lastOrNull { it.isNotEmpty() }?.let(MqttChatCategory::fromTopicName)
        } else {
            null
        }
    }
}
