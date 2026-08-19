package sky.kr.co.newtogetusa.data.remote.request.player

data class PlayerAreaAddRequest(
    val area_id: Int? = null,
    val is_domestic: Boolean = true,
    val enable: Boolean = true,
    val area_type: String = "BASIC",
    val areaType: String = area_type,
    val domestic_yn: String = if (is_domestic) "Y" else "N",
    val use_yn: String = if (enable) "Y" else "N",
    val depart: PlayerAreaLocationRequest,
    val dest: PlayerAreaLocationRequest,
)

data class PlayerAreaAddedRequest(
    val area_id: Int? = null,
    val start: String,
    val end: String,
    val enable: Boolean = true,
    val is_domestic: Boolean = true,
    val dest: PlayerAreaLocationRequest,
)

data class PlayerAreaDeleteRequest(
    val area_id: Int
)

data class PlayerAreaLocationRequest(
    val address: String,
    val address2: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val range: Int = 10,
)
