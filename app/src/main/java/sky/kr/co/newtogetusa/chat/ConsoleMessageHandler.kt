package sky.kr.co.newtogetusa.chat

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import javax.inject.Inject

class ConsoleMessageHandler @Inject constructor() : MessageHandler{
    override fun handleIncomingMessage(sender: String, content: String) {
        TODO("Not yet implemented")
    }

    override fun onConnectionLost(cause: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
        TODO("Not yet implemented")
    }
}