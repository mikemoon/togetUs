package sky.kr.co.newtogetusa.data.remote.dto.kakao

data class KakaoKeywordResponse(
    val meta: Meta,
    val documents: List<Place>
) {
    data class Meta(
        val total_count: Int,
        val pageable_count: Int,
        val is_end: Boolean,
        val same_name: SameName?
    )
    data class SameName(
        val region: List<String>,
        val keyword: String,
        val selected_region: String
    )
    data class Place(
        val id: String?,
        val place_name: String?,
        val category_name: String?,
        val category_group_code: String?,
        val category_group_name: String?,
        val phone: String?,
        val address_name: String?,
        val road_address_name: String?,
        val x: String?,     // 경도
        val y: String?,     // 위도
        val place_url: String?,
        val distance: String? // m (x,y 지정했을 때만 내려옴)
    )
}
