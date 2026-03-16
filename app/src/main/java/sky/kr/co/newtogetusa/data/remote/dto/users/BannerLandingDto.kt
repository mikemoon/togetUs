package sky.kr.co.newtogetusa.data.remote.dto.users

import com.google.gson.annotations.SerializedName

data class BannerLandingDto(

    @SerializedName("landing_type")
    val landingType: String,

    @SerializedName("landing_url")
    val landingUrl: String
)