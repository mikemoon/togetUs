package sky.kr.co.newtogetusa.data.remote.dto.users

import com.google.gson.annotations.SerializedName

data class BannerDto(

    @SerializedName("bn_id")
    val bannerId: Int,

    val title: String,

    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("landing_type")
    val landingType: String,

    @SerializedName("landing_url")
    val landingUrl: String,

    @SerializedName("background_color")
    val backgroundColor: String
)