package sky.kr.co.newtogetusa.data.remote.dto.users

data class PlayerInfoDto(
    val user: Player? = null,
    val player: Player? = null,
    val reg_date: String? = null,
    val stars: Int = 0,
    val delivery_id: Int? = null,
    val depart_address: String? = null,
    val dest_address: String? = null,
    val contents: String? = null,
    val items: List<String> = emptyList()
) {
    data class Player(
        val player_id: Int?,
        val user_id: Int? = null,
        val nickname: String? = null,
        val profile_image: String?,
        val enable: Boolean? = null
    )
}
