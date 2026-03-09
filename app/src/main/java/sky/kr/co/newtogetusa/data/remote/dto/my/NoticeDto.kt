package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class NoticeDto(
    @SerializedName("notice_id")
    val noticeId: Int,

    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("reg_date")
    val regDate: String,
    val title: String,
    val contents: String
)