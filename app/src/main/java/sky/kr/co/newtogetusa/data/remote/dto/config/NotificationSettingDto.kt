package sky.kr.co.newtogetusa.data.remote.dto.config

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import sky.kr.co.newtogetusa.data.remote.dto.YnBooleanJsonAdapter

data class NotificationSettingDto(

    @JsonAdapter(YnBooleanJsonAdapter::class)
    @SerializedName("push_yn")
    val pushYn: Boolean,

    @JsonAdapter(YnBooleanJsonAdapter::class)
    @SerializedName("chat_yn")
    val chatYn: Boolean,

    @JsonAdapter(YnBooleanJsonAdapter::class)
    @SerializedName("marketing_yn")
    val marketingYn: Boolean,

    @JsonAdapter(YnBooleanJsonAdapter::class)
    @SerializedName("delivery_yn")
    val deliveryYn: Boolean,

    @JsonAdapter(YnBooleanJsonAdapter::class)
    @SerializedName("night_push_yn")
    val nightPushYn: Boolean,

    @JsonAdapter(YnBooleanJsonAdapter::class)
    @SerializedName("marketing_agree_yn")
    val marketingAgreeYn: Boolean?,

    @SerializedName("marketing_agree_date")
    val marketingAgreeDate: String?,

    @SerializedName("marketing_deagree_date")
    val marketingDeagreeDate: String?
)
