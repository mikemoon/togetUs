package sky.kr.co.newtogetusa.data.remote.request.delivery

import com.google.gson.annotations.SerializedName

data class DeliveryPayRequest(
    @SerializedName("player_id")
    val playerId: Long,
    @SerializedName("fee_adjust")
    val feeAdjust: Long,
    @SerializedName("fee_final")
    val feeFinal: Long,
    @SerializedName("pay_type")
    val payType: String = "credit_card",
    @SerializedName("payment_id")
    val paymentId: String,
    @SerializedName("pickup")
    val pickup: DeliveryPayPickupRequest,
    @SerializedName("terms_cds")
    val termsCodes: List<String> = listOf("term_1", "term_2", "term_3")
)

data class DeliveryPayPickupRequest(
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String
)
