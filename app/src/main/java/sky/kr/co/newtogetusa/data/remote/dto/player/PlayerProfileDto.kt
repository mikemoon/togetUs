package sky.kr.co.newtogetusa.data.remote.dto.player

data class PlayerProfileDto(
    val player_id: Int? = null,
    val player: Player,
    val is_like: Boolean? = null,
    val rating: Rating? = null,
    val evaluation: Evaluation? = null,
    val introduction: String? = null,
    val areas: Areas? = null,
    val areas_basic: List<PlayerArea>? = null,
    val areas_added: List<PlayerArea>? = null
) {
    data class Player(
        val player_id: Int,
        val user_id: Int? = null,
        val nickname: String,
        val profile_image: String,
        val enable: Boolean
    )

    data class Rating(
        val player_id: Int,
        val star_average: Double,
        val star_count: Int,
        val complete_count: Int,
        val cancel_count: Int,
        val review_count: Int
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

    data class PlayerArea(
        val player_area_id: Int? = null,
        val player_id: Int? = null,
        val area_type: String? = null,
        val depart_address: String? = null,
        val dest_address: String? = null,
        val start_date: String? = null,
        val end_date: String? = null,
        val depart_address2: String? = null,
        val dest_address2: String? = null,
        val domestic_yn: String? = null,
        val use_yn: String? = null
    )
}
