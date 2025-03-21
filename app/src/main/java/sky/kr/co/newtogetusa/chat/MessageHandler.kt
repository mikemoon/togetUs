package sky.kr.co.newtogetusa.chat

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken

interface MessageHandler {
    fun handleIncomingMessage(sender: String, content: String)
    fun onConnectionLost(cause: Throwable?)
    fun onDeliveryComplete(token: IMqttDeliveryToken?)
}