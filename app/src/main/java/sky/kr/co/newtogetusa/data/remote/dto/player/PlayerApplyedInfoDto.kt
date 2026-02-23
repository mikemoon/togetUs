package sky.kr.co.newtogetusa.data.remote.dto.player

data class PlayerApplyedInfoDto(
    val user_id: Int,
    val player_id: Int?,
    val verify_phone: Boolean,
    val verify_bank: Boolean,
    val terms: List<String>,
    val bank: BankDto?,
    val areas: List<AreaDto>,
    val profile_image: String?,
    val introduction: String?,
    val criminalrecord_file_name: String?,
    val certi_req_date: String?,
    val certi_res_date: String?
)

data class BankDto(
    val bank_cd: String?,
    val bank_name: String?,
    val account_number: String?,
    val account_depositor: String?
)

data class AreaDto(
    val depart: LocationDto?,
    val dest: LocationDto?
)

data class LocationDto(
    val address: String?,
    val address2: String?,
    val latitude: Double?,
    val longitude: Double?,
    val range: Int?
)