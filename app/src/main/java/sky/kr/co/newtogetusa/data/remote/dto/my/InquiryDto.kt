package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class InquiryDto(

    @SerializedName("one_id")
    val oneId: Int,

    val status: String,
    val title: String,
    val inquiry: String,

    @SerializedName("inquiry_phone")
    val inquiryPhone: String,

    @SerializedName("inquiry_attachs")
    val inquiryAttachs: List<String>,

    @SerializedName("inquiry_date")
    val inquiryDate: String,

    val response: String?,

    @SerializedName("response_attachs")
    val responseAttachs: List<String>,

    @SerializedName("response_date")
    val responseDate: String
)