package sky.kr.co.newtogetusa.data.remote.dto.delivery

import com.google.gson.annotations.SerializedName

data class DeliveryFeeResponse(
    @SerializedName("delivery_id")
    val deliveryId: Long,

    @SerializedName("expected_straight")
    val expectedStraight: Int,   // 예상 직선 거리 (km 등)

    @SerializedName("expected_weight_cd")
    val expectedWeightCd: String, // small / medium / big

    @SerializedName("fee_basic")
    val feeBasic: Int,           // 기본 요금

    @SerializedName("fee_adjust")
    val feeAdjust: Int,          // 조정 요금 (+/-)

    @SerializedName("fee_final")
    val feeFinal: Int            // 최종 요금
)