package sky.kr.co.newtogetusa.data.remote.dto.delivery

data class DeliveryDetailResponse(
    val delivery_id: Long,
    val requester_id: Long,
    val player_id: Long?,
    val status_cd: String,
    val is_like: Boolean,
    val is_apply: Boolean = false,

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
    val apply_date: String?,

    // 유저 수령확인 여부
    val is_confirm: Boolean = false,
    val user_review: DeliveryReviewDto? = null,
    val player_review: DeliveryReviewDto? = null,
    val complete_picture: DeliveryProofPictureDto? = null
) {
    /** 동행완료 증빙이 존재하는지 (완료 촬영 URL 또는 촬영불가 사유코드).
     *  false면 유저가 먼저 배송완료 처리한 케이스라 [동행 완료 촬영] 버튼을 노출해야 한다. */
    val hasCompleteProof: Boolean
        get() = !complete_picture?.complete_picture.isNullOrEmpty() ||
            !complete_picture?.no_picture_cd.isNullOrEmpty()
}

// 픽업/동행완료 증빙 촬영 정보
data class DeliveryProofPictureDto(
    val pickup_picture: String? = null,
    val complete_picture: String? = null,
    val no_picture_cd: String? = null
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
