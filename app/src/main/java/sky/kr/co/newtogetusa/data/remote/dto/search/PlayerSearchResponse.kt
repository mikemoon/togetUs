package sky.kr.co.newtogetusa.data.remote.dto.search

data class PlayerSearchResponse(
    val page_no: Int,
    val total_cnt: Int,
    val has_more: Boolean,
    val players: List<PlayerDto>
)

data class PlayerDto(
    val player_id: Long,
    val user_id: Long,
    val nickname: String,
    val profile_image: String?,
    val enable: Boolean,
    val distance: Double,
    val star_average: Int,
    val complete_count: Int,
    val lastest_date: String?
)