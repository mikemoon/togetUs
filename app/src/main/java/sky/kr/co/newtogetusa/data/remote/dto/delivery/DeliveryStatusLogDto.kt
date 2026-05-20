package sky.kr.co.newtogetusa.data.remote.dto.delivery

import com.google.gson.annotations.SerializedName

data class DeliveryStatusLogDto(
    val actor: String? = null,
    @SerializedName("actor_id")
    val actorId: Long? = null,
    @SerializedName("status_cd")
    val statusCd: String? = null,
    @SerializedName("status_name")
    val statusName: String? = null,
    @SerializedName("reg_date")
    val regDate: String? = null,
    val pictures: List<String>? = emptyList(),
)
