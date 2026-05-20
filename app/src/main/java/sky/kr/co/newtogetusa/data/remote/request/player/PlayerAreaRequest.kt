package sky.kr.co.newtogetusa.data.remote.request.player

data class PlayerAreaAddRequest(
    val is_domestic: Boolean = true,
    val enable: Boolean = true,
    val depart: PlayerAreaLocationRequest,
    val dest: PlayerAreaLocationRequest,
)

data class PlayerAreaAddedRequest(
    val area_id: Int? = null,
    val start: String,
    val end: String,
    val enable: Boolean = true,
    val depart: PlayerAreaLocationRequest,
    val dest: PlayerAreaLocationRequest,
)

data class PlayerAreaLocationRequest(
    val address: String,
    val address2: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val range: Int = 10,
)
