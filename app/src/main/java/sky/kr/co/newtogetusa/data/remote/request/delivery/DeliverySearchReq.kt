package sky.kr.co.newtogetusa.data.remote.request.delivery

data class DeliverySearchReq(
    val type: String,
    val title: String,
    val page_no: Int
)