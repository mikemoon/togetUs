package sky.kr.co.newtogetusa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.main.chat.ChatRoomForegroundTracker
import sky.kr.co.newtogetusa.ui.main.chat.ChatRoomListUpdateBus
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TogetUsFirebaseMessagingService : FirebaseMessagingService(){

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    @Inject
    lateinit var configRepository: ConfigRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            dataStoreRepository.putString(DataStoreKey.KEY_FCM_TOKEN, token)
            registerDeviceToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val roomId = extractRoomId(data)
        val isChatPush = isChatPush(data, roomId)

        if (isChatPush && roomId > 0L) {
            ChatRoomListUpdateBus.notifyRoomUpdated(roomId)
        }

        if (isChatPush && roomId > 0L && ChatRoomForegroundTracker.activeRoomId == roomId) {
            Timber.d("[Push] same chat room, suppress notification roomId=$roomId")
            return
        }

        showNotification(
            title = message.notification?.title ?: data["title"] ?: getString(R.string.app_name),
            body = message.notification?.body ?: data["body"] ?: data["message"].orEmpty(),
            roomId = roomId.takeIf { isChatPush && it > 0L }
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun registerDeviceToken(token: String) {
        if (token.isBlank()) return
        when (val res = configRepository.postPushToken(hashMapOf("device_token" to token, "device_type" to "AOS"))) {
            is ResultWrapper.Success -> Timber.d("FCM token registered")
            else -> Timber.e("FCM token register failed $res")
        }
    }

    private fun isChatPush(data: Map<String, String>, roomId: Long): Boolean =
        data["type"]?.equals("chat", ignoreCase = true) == true || roomId > 0L

    private fun extractRoomId(data: Map<String, String>): Long =
        data["room_id"]?.toLongOrNull()
            ?: data["roomId"]?.toLongOrNull()
            ?: -1L

    private fun showNotification(title: String, body: String, roomId: Long?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(manager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            roomId?.let {
                putExtra(MainActivity.EXTRA_PUSH_ROOM_ID, it)
                putExtra(MainActivity.EXTRA_PUSH_TYPE, PUSH_TYPE_CHAT)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            roomId?.hashCode() ?: System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chat_enable)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(roomId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "TogetUs",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "togetus_push"
        private const val PUSH_TYPE_CHAT = "chat"
    }
}
