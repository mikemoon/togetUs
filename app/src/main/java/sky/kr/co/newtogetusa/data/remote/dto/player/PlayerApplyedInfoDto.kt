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
    val player_area_id: Int?,
    val player_id: Int?,

    val depart_area_cd: String?,
    val depart_area_gu_cd: String?,
    val depart_address: String?,
    val depart_address2: String?,
    val depart_latitude: Double?,
    val depart_longitude: Double?,
    val depart_range: Int?,

    val dest_area_cd: String?,
    val dest_area_gu_cd: String?,
    val dest_address: String?,
    val dest_address2: String?,
    val dest_latitude: Double?,
    val dest_longitude: Double?,
    val dest_range: Int?,

    val domestic_yn: String?,
    val use_yn: String?
)