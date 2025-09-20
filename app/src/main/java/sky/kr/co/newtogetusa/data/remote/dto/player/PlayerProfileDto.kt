package sky.kr.co.newtogetusa.data.remote.dto.player

data class PlayerProfileDto(
    val player: Player,
    val evaluation: Evaluation,
    val introduction: String,
    val areas: Areas
) {
    data class Player(
        val player_id: Int,
        val nickname: String,
        val profile_image: String,
        val enable: Boolean
    )

    data class Evaluation(
        val start_average: Int,
        val start_count: Int,
        val complete_count: Int,
        val cancel_count: Int,
        val review_count: Int
    )

    data class Areas(
        val departure: AreaInfo,
        val departureDetail: AreaInfo,
        val arrival: AreaInfo,
        val arrivalDetail: AreaInfo,
        val oversea: AreaInfo
    )

    data class AreaInfo(
        val code: String,
        val name: String
    )
}