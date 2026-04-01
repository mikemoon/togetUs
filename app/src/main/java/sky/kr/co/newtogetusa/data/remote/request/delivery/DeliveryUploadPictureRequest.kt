package sky.kr.co.newtogetusa.data.remote.request.delivery

data class DeliveryUploadPictureRequest(
    val mime: String,
    val base64: String,
    val latitude: Double,
    val longitude: Double,
    val picture_date: String,
    val no_picture_cd: String,
    val no_picture_reason: String
)