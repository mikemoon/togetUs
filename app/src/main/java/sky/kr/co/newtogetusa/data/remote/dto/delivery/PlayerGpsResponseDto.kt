package sky.kr.co.newtogetusa.data.remote.dto.delivery

// iOS DeliveryMapViewModel getPlayerGps 대응: 플레이어 GPS 조회 응답
data class PlayerGpsResponseDto(
    val members: List<PlayerGpsMemberDto> = emptyList()
)

data class PlayerGpsMemberDto(
    val user_id: Long = 0,
    val nickname: String = "",
    val gps_latitude: Double = 0.0,
    val gps_longitude: Double = 0.0,
    val gps_date: String = ""
)
