package sky.kr.co.newtogetusa.chat

import android.content.Context
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import timber.log.Timber
import javax.inject.Inject

class AndroidMessageHandler @Inject constructor(
    private val context: Context,
    private val messageCallback: (String, String) -> Unit
): MessageHandler {
    override fun handleIncomingMessage(sender: String, content: String) {
        messageCallback(sender, content)
    }

    override fun onConnectionLost(cause: Throwable?) {
        Timber.d(cause, "MQTT connection lost")
    }

    override fun onDeliveryComplete(token: IMqttDeliveryToken?) {
        Timber.d("MQTT delivery complete. messageId=%s", token?.messageId)
    }

}
