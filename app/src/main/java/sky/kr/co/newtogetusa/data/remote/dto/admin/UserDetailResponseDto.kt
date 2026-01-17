package sky.kr.co.newtogetusa.data.remote.dto.admin

data class UserDetailResponseDto(
    val user: UserDto,
    val devices: List<DeviceDto>,
    val recent_logs: List<RecentLogDto>,
    val roles: List<RoleDto>,
    val terms: List<TermDto>,
    val tokems: TokemsDto
)

data class UserDto(
    val reg_date: String,
    val chg_date: String,
    val user_id: Long,
    val provider: String,
    val nickname: String,
    val oauth_name: String,
    val email_email: String,
    val oauth_email: String,
    val use_yn: String,
    val last_login_date: String,
    val join_date: String
)

data class DeviceDto(
    val reg_date: String,
    val chg_date: String,
    val user_id: Long,
    val device: String,
    val os_cd: String
)

data class RecentLogDto(
    val user_id: Long,
    val api: String,
    val contents: String,
    val reg_date: String
)

data class RoleDto(
    val reg_date: String,
    val chg_date: String,
    val user_id: Long,
    val role_cd: String,
    val role_name: String
)

data class TermDto(
    val reg_date: String,
    val chg_date: String,
    val user_id: Long,
    val term_cd: String,
    val term_name: String,
    val agree_yn: String,
    val agree_date: String,
    val deagree_date: String
)

data class TokemsDto(
    val reg_date: String,
    val chg_date: String,
    val user_id: Long,
    val access_token: String,
    val access_expire: String,
    val refresh_token: String,
    val refresh_expire: String
)