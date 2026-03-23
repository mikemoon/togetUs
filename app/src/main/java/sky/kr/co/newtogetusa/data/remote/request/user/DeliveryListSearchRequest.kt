package sky.kr.co.newtogetusa.data.remote.request.user

data class DeliveryListSearchRequest(
    val myarea: Boolean,
    val depart_cd: List<String>,
    val dest_cd: List<String>,
    val sort_type: String,
    val fee: Int,
    val face2face: Boolean?,   // nullable
    val immediately: String,
    val page_no: Int,
    val page_size: Int
)