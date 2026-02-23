package sky.kr.co.newtogetusa.data.remote.dto

data class BaseDto(
    @com.google.gson.annotations.SerializedName("code")
    val code: String,
    @com.google.gson.annotations.SerializedName("cate")
    val cate: String,
    @com.google.gson.annotations.SerializedName("name")
    val name: String,
    @com.google.gson.annotations.SerializedName("description")
    val description: String
)