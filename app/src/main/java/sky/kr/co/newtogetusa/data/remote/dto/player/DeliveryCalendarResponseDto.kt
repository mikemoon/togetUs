package sky.kr.co.newtogetusa.data.remote.dto.player

import com.google.gson.annotations.SerializedName

data class DeliveryCalendarResponseDto(
    val calender: List<CalendarDateDto>
)

data class CalendarDateDto(
    val date: String,
    val deliveries: List<CalendarDeliveryDto>
)

data class CalendarDeliveryDto(

    val date: String,

    @SerializedName("delivery_id")
    val deliveryId: Int,

    val status: String
)