package sky.kr.co.newtogetusa.data.remote.request.player

data class PlayerJoinRequest(
    val bank: BankRequest,
    val profile_image: ProfileImageRequest,
    val introduction: String,
    val criminal: CriminalRequest,
    val areas: List<AreaRequest>
)

data class BankRequest(
    val term_cds: List<String>,
    val bank_cd: String,
    val account_number: String,
    val account_depositor: String
)

data class ProfileImageRequest(
    val mime: String,
    val base64: String
)

data class CriminalRequest(
    val filename: String,
    val base64: String
)

data class AreaRequest(
    val depart: LocationRequest,
    val dest: LocationRequest
)

data class LocationRequest(
    val address: String,
    val address2: String,
    val latitude: Double,
    val longitude: Double,
    val range: Int
)