package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class FAQDto(
    @SerializedName("cate_id")
    val cateId: Int,

    @SerializedName("cate_name")
    val cateName: String,

    @SerializedName("faq_id")
    val faqId: Int,

    val title: String,
    val contents: String
)