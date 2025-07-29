package sky.kr.co.newtogetusa.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChangeEmailPasswordResponse(
    @field:SerializedName("user_id") val userId: Int,
    @field:SerializedName("verify_code") val verifyCode: String,
)