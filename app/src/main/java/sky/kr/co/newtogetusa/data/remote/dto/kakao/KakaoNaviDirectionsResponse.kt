package sky.kr.co.newtogetusa.data.remote.dto.kakao

data class KakaoNaviDirectionsResponse(
    val routes: List<Route> = emptyList()
) {
    data class Route(
        val summary: Summary?,
        val sections: List<Section> = emptyList(),
        val result_code: Int,
        val result_msg: String?
    )

    data class Summary(
        val distance: Int?,
        val duration: Int?,
        val bound: Bound?
    )

    data class Bound(
        val min_x: Double, val min_y: Double,
        val max_x: Double, val max_y: Double
    )

    data class Section(
        val distance: Int?,
        val duration: Int?,
        val roads: List<Road> = emptyList()
    )

    data class Road(
        val name: String?,
        val distance: Int?,
        val duration: Int?,
        val vertexes: List<Double> = emptyList() // [x1,y1,x2,y2,...]
    )
}