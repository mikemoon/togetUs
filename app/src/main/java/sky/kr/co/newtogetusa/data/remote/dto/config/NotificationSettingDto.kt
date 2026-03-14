package sky.kr.co.newtogetusa.data.remote.dto.config

import com.google.gson.annotations.SerializedName

data class NotificationSettingDto(

    @SerializedName("push_yn")
    val pushYn: Boolean,

    @SerializedName("chat_yn")
    val chatYn: Boolean,

    @SerializedName("marketing_yn")
    val marketingYn: Boolean,

    @SerializedName("delivery_yn")
    val deliveryYn: Boolean,

    @SerializedName("night_push_yn")
    val nightPushYn: Boolean,

    @SerializedName("marketing_agree_yn")
    val marketingAgreeYn: Boolean?,

    @SerializedName("marketing_agree_date")
    val marketingAgreeDate: String?,

    @SerializedName("marketing_deagree_date")
    val marketingDeagreeDate: String?
)
