package sky.kr.co.newtogetusa.chat

import android.content.Context
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import javax.inject.Inject

class AndroidMessageHandler @Inject constructor(
    private val context: Context,
    private val messageCallback: (String, String) -> Unit
): MessageHandler {
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