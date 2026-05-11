package sky.kr.co.newtogetusa.chat

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import timber.log.Timber
import javax.inject.Inject

class ConsoleMessageHandler @Inject constructor() : MessageHandler{
    override fun handleIncomingMessage(sender: String, content: String) {
        Timber.d("MQTT message received. sender=%s content=%s", sender, content)
    }

    override fun onConnectionLost(cause: Throwable?) {
        Timber.d(cause, "MQTT connection lost")
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
        Timber.d("MQTT delivery complete. messageId=%s", token?.messageId)
    }
}
