package sky.kr.co.newtogetusa.data.remote.dto.player

import com.google.gson.annotations.SerializedName

data class DeliveryHistoryResponseDto(

    @SerializedName("page_no")
    val pageNo: Int,

    @SerializedName("total_cnt")
    val totalCnt: Int,

    @SerializedName("has_more")
    val hasMore: Boolean,

    val deliveries: List<DeliverySummaryDto>
)

data class DeliverySummaryDto(

    @SerializedName("delivery_id")
    val deliveryId: Int,

    @SerializedName("requester_id")
    val requesterId: Int,

    @SerializedName("player_id")
    val playerId: Int?,

    val title: String,

    @SerializedName("status_cd")
    val statusCd: String,

    @SerializedName("prd_picture")
    val prdPicture: String?,

    @SerializedName("depart_address")
    val departAddress: String,

    @SerializedName("dest_address")
    val destAddress: String,

    @SerializedName("pickup_immediately")
    val pickupImmediately: Boolean,

    @SerializedName("pickup_date")
    val pickupDate: String,

    @SerializedName("fee_final")
    val feeFinal: Int,

    @SerializedName("regist_date")
    val registDate: String?,

    @SerializedName("apply_date")
    val applyDate: String?
)