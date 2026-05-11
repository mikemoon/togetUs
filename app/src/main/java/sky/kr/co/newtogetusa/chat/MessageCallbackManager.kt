package sky.kr.co.newtogetusa.chat

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageCallbackManager @Inject constructor() {
    private val callbacks = CopyOnWriteArrayList<MessageHandler>()

    fun registerCallback(callback: MessageHandler) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun unregisterCallback(callback: MessageHandler) {
        callbacks.remove(callback)
    }

    fun notifyMessageReceived(sender: String, content: String) {
        callbacks.forEach { it.handleIncomingMessage(sender, content) }
    }

    fun notifyConnectionLost(cause: Throwable?) {
        callbacks.forEach { it.onConnectionLost(cause) }
    }

    fun notifyDeliveryComplete(token: IMqttDeliveryToken?) {
        callbacks.forEach { it.onDeliveryComplete(token) }
    }
}
