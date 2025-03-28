package sky.kr.co.newtogetusa.data.remote

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber

abstract class BaseNetRepo {
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> ResultWrapper.NetworkError
                    is HttpException -> {
                        val httpCode = throwable.code().toString()
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Timber.d("http error: $httpCode , body: ${errorBody.toString()}")
                        try {
                            val gsonErrorBody = Gson().fromJson(
                                errorBody,
                                ErrorBody::class.java
                            )
                            if(gsonErrorBody != null) {
                                val code = gsonErrorBody.code
                                val message = gsonErrorBody.msg
                                val dataObj = gsonErrorBody.dataObj
                                ResultWrapper.GenericError(code, message, dataObj)
                            }else{
                                ResultWrapper.GenericError(httpCode, "", null)
                            }
                        }catch (e : JsonSyntaxException){
                            ResultWrapper.GenericError(null, null, null)
                        }
                    }
                    else -> {
                        Timber.e("throwable: $throwable")
                        ResultWrapper.GenericError(null, throwable.message, null)
                    }
                }
            }
        }
    }
}