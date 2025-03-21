package sky.kr.co.newtogetusa.chat

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.inject.Inject

class MqttChatClientImpl @Inject constructor(
    private val config: MqttConnectionConfig,
    private val messageHandler: MessageHandler
): ChatClient{
    private val client: MqttClient
    private val connectOptions = MqttConnectOptions()
    private val persistence = MemoryPersistence()

    init {
        client = MqttClient(config.brokerUrl, config.clientId, persistence)
        connectOptions.apply {
            isCleanSession = true
            connectionTimeout = 60
            isAutomaticReconnect = true
            userName = config.username
        }

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                messageHandler.onConnectionLost(cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let {
                    val payload = String(it.payload)
                    if (topic == config.chatTopic) {
                        try {
                            val parts = payload.split(":", limit = 2)
                            if (parts.size == 2) {
                                val sender = parts[0].trim()
                                val content = parts[1].trim()
                                messageHandler.handleIncomingMessage(sender, content)
                            } else {
                                messageHandler.handleIncomingMessage("알 수 없음", payload)
                            }
                        } catch (e: Exception) {
                            println("메시지 처리 중 오류: ${e.message}")
                        }
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                messageHandler.onDeliveryComplete(token)
            }
        })
    }

    override fun connect() {
        try {
            println("MQTT 브로커에 연결 중...")
            client.connect(connectOptions)
            client.subscribe(config.chatTopic, 1) // QoS 1로 구독
            println("채팅방 '${config.chatTopic}'에 연결되었습니다.")
        } catch (e: MqttException) {
            println("연결 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun disconnect() {
        try {
            if (client.isConnected) {
                client.disconnect()
                println("연결이 종료되었습니다.")
            }
        } catch (e: MqttException) {
            println("연결 종료 중 오류: ${e.message}")
        }
    }

    override fun sendMessage(message: String) {
        try {
            val content = "${config.username}: $message"
            val mqttMessage = MqttMessage(content.toByteArray())
            mqttMessage.qos = 1
            client.publish(config.chatTopic, mqttMessage)
        } catch (e: MqttException) {
            println("메시지 전송 실패: ${e.message}")
        }
    }

    override fun isConnected(): Boolean {
        return client.isConnected
    }
}