package sky.kr.co.newtogetusa.data.remote.dto.users

data class DeliverySearchListResponse(
    val page_no: Int,
    val total_cnt: Int,
    val has_more: Boolean,
    val deliveries: List<DeliveryItem>
)

data class DeliveryItem(
    val delivery_id: Long,
    val requester_id: Long,
    val player_id: Long?,          // nullable
    val title: String,
    val status_cd: String,
    val prd_picture: String?,      // nullable
    val depart_address: String,
    val dest_address: String,
    val pickup_immediately: Boolean,
    val pickup_date: String,       // "yyyyMMdd HHmm"
    val fee_final: Int,
    val regist_date: String?,      // nullable
    val apply_date: String?        // nullable
)