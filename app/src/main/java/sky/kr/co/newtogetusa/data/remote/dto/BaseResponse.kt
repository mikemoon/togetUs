package sky.kr.co.newtogetusa.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @field:SerializedName("resultCode") val resultCode: Int,
    @field:SerializedName("resultMsg") val resultMsg: String,
    @field:SerializedName("data") val data: T?
)
