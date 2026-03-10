package sky.kr.co.newtogetusa.data.remote.dto.my

import com.google.gson.annotations.SerializedName

data class FAQCateDto(
    @SerializedName("cate_id")
    val cateId: Int,

    @SerializedName("cate_name")
    val cateName: String,
)
