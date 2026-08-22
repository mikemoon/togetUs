package sky.kr.co.newtogetusa.data.remote.request.player

import com.google.gson.annotations.SerializedName

data class PlayerDeliveryHistoryReq(
    val type: String,

    val title: String,

    @SerializedName("page_no")
    val pageNo: Int,

    @SerializedName("page_size")
    val pageSize: Int,

    @SerializedName("year_month")
    val yearMonth: String? = null,

    val os: String = "A"
)
