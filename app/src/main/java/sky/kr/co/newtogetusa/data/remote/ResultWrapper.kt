package sky.kr.co.newtogetusa.data.remote

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()
    data class GenericError(val code: String?, val message: String?, val errorData: ErrorData?) : ResultWrapper<Nothing>()
    object NetworkError : ResultWrapper<Nothing>()
}