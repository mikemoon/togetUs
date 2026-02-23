package sky.kr.co.newtogetusa.data.remote.dto.delivery

data class DeliveryDetailResponse(
    val delivery_id: Long,
    val requester_id: Long,
    val player_id: Long?,
    val status_cd: String,
    val is_like: Boolean,

    val title: String,
    val is_domestic: Boolean,

    val depart: AddressDto,
    val depart_contact: ContactDto,

    val dest: AddressDto,
    val dest_contact: ContactDto,

    val pickup: PickupDto,
    val product: ProductDto,

    val player_profile: PlayerProfileDto?,
    val requester_rating: RequesterRatingDto,

    val expected: ExpectedDto,
    val fee: FeeDto,

    val pay: List<PayDto>,

    val regist_date: String,
    val apply_date: String?
)

data class AddressDto(
    val address: String,
    val address2: String?,
    val latitude: Double,
    val longitude: Double
)

data class ContactDto(
    val name: String?,
    val phone: String?
)

data class PickupDto(
    val date: String,      // "20260122"
    val time: String,      // "1951"
    val is_immediately: Boolean,
    val is_face2face: Boolean
)

data class ProductDto(
    val name: String,
    val type_cd: String,
    val weight_cd: String,
    val volume_cd: String,
    val descript: String,
    val pictures: List<String>
)

data class RequesterRatingDto(
    val user_id: Long,
    val nickname: String,
    val star_rating: Double,
    val deliveries: Int
)

data class ExpectedDto(
    val expected_distance: Int,
    val expected_time: Int
)

data class FeeDto(
    val fee_basic: Int,
    val fee_adjust: Int,
    val fee_final: Int
)

data class PayDto(
    val type: String? = null,
    val amount: Int? = null
)

data class PlayerProfileDto(
    val user_id: Long,
    val nickname: String,
    val profile_image: String?
)