package sky.kr.co.newtogetusa.data.remote.request.delivery

data class DeliveryReviewRequest(
    val stars: Int,
    val contents: String,
    val items: List<String>,
)
