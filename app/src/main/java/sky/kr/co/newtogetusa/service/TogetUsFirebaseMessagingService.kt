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
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRefreshBus
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
        val deliveryId = extractDeliveryId(data)

        if (isChatPush && roomId > 0L) {
            ChatRoomListUpdateBus.notifyRoomUpdated(roomId)
        }

        // 동행(배송) 푸시: 포그라운드 수신 시 열린 상세/목록 화면 갱신 (iOS handlePushReceived 대응)
        if (deliveryId > 0L) {
            serviceScope.launch {
                if (!dataStoreRepository.getString(DataStoreKey.KEY_TOKEN).isNullOrBlank()) {
                    DeliveryRefreshBus.notifyDeliveryUpdated(deliveryId)
                }
            }
        }

        if (isChatPush && roomId > 0L && ChatRoomForegroundTracker.activeRoomId == roomId) {
            Timber.d("[Push] same chat room, suppress notification roomId=$roomId")
            return
        }

        showNotification(
            title = message.notification?.title ?: data["title"] ?: getString(R.string.app_name),
            body = message.notification?.body ?: data["body"] ?: data["message"].orEmpty(),
            roomId = roomId.takeIf { isChatPush && it > 0L },
            deliveryId = deliveryId.takeIf { isDeliveryDetailPush(data) && it > 0L }
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

    // 동행상세 푸시 여부 (landing_type == "DELIVERY_DETAIL")
    private fun isDeliveryDetailPush(data: Map<String, String>): Boolean =
        data["landing_type"]?.equals("DELIVERY_DETAIL", ignoreCase = true) == true

    // delivery_id ?? landing_id (숫자 문자열 허용)
    private fun extractDeliveryId(data: Map<String, String>): Long =
        data["delivery_id"]?.toLongOrNull()
            ?: data["landing_id"]?.toLongOrNull()
            ?: -1L

    private fun showNotification(title: String, body: String, roomId: Long?, deliveryId: Long? = null) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(manager)

        val isChatPush = roomId != null && roomId > 0L
        val isDeliveryPush = deliveryId != null && deliveryId > 0L
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_HOME_FROM_PUSH, true)
            if (isChatPush) {
                putExtra(MainActivity.EXTRA_PUSH_TYPE, MainActivity.PUSH_TYPE_CHAT)
                putExtra(MainActivity.EXTRA_PUSH_ROOM_ID, roomId)
            } else if (isDeliveryPush) {
                putExtra(MainActivity.EXTRA_PUSH_TYPE, MainActivity.PUSH_TYPE_DELIVERY_DETAIL)
                putExtra(MainActivity.EXTRA_PUSH_DELIVERY_ID, deliveryId)
            }
        }

        val notificationId = roomId?.hashCode()
            ?: deliveryId?.hashCode()
            ?: System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
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

        manager.notify(notificationId, notification)
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
    }
}
