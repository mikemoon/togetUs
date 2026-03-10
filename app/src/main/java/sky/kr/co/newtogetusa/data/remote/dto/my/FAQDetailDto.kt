package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class FAQDetailDto(
    @SerializedName("faq_id")
    val faqId: Int,

    @SerializedName("mode_type")
    val modeType: String,

    @SerializedName("reg_date")
    val regDate: String,
    val title: String,
    val contents: String,

)