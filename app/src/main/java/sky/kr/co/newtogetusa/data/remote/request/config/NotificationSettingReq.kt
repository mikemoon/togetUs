package sky.kr.co.newtogetusa.data.remote.request.config

import com.google.gson.annotations.SerializedName

data class NotificationSettingReq(

    @SerializedName("push_yn")
    val pushYn: String,

    @SerializedName("chat_yn")
    val chatYn: String,

    @SerializedName("marketing_yn")
    val marketingYn: String,

    @SerializedName("delivery_yn")
    val deliveryYn: String,

    @SerializedName("night_push_yn")
    val nightPushYn: String
)