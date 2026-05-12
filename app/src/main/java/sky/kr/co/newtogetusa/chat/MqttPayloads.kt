package sky.kr.co.newtogetusa.chat

import com.google.gson.annotations.SerializedName

data class MqttMessagePayload(
    @field:SerializedName("room_id") val roomId: Long,
    @field:SerializedName("msg_id") val messageId: Long = 0,
    @field:SerializedName("msg_ptr_id") val messagePointerId: Long = 0,
    @field:SerializedName("send_uid") val senderUserId: Long = 0,
    @field:SerializedName("send_uname") val senderUserName: String? = null,
    @field:SerializedName("send_date") val sendDate: String? = null,
    @field:SerializedName("mimetype") val mimeType: String = "text/plain",
    @field:SerializedName("msg") val message: String
)

data class MqttReadPayload(
    @field:SerializedName("room_id") val roomId: Long,
    @field:SerializedName("read_uid") val readUserId: Long = 0,
    @field:SerializedName("msg_id") val messageId: Long,
    @field:SerializedName("read_date") val readDate: String? = null
)

data class MqttGpsPayload(
    @field:SerializedName("room_id") val roomId: Long,
    @field:SerializedName("send_uid") val senderUserId: Long = 0,
    @field:SerializedName("latitude") val latitude: Double,
    @field:SerializedName("longitude") val longitude: Double,
    @field:SerializedName("send_date") val sendDate: String? = null
)
