package sky.kr.co.newtogetusa.data.remote.request.player

data class PlayerSearchRequest(
    val depart_cd: List<String>,
    val dest_cd: List<String>,
    val sort_type: String,
    val page_no: Int,
    val page_size: Int
)