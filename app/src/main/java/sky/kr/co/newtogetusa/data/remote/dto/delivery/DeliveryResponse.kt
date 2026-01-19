package sky.kr.co.newtogetusa.data.remote.dto.delivery

import com.google.gson.annotations.SerializedName

data class DeliveryResponse(
    @SerializedName("delivery_id")
    val deliveryId: Long
)