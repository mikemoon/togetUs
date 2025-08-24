package sky.kr.co.newtogetusa.data.remote

data class ErrorBody(
    val code: String?,
    val msg: String?,
    val dataObj: ErrorData?
)

data class ErrorData(
    val httpCode: Int?,
    val type: String?,
    val error: String?,
    val message: String?,
    val user_id: Int,
    val verify_code: String?,
)