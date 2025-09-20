package sky.kr.co.newtogetusa.data.remote.dto.users

data class PlayerInfoDto(
    val player: Player,
    val reg_date: String,
    val stars: Int,
    val delivery_id: Int,
    val depart_address: String,
    val dest_address: String,
    val contents: String,
    val items: List<String>
) {
    data class Player(
        val player_id: Int,
        val nickname: String,
        val profile_image: String
    )
}