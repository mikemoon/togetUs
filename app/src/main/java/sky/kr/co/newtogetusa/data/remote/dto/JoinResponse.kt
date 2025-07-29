package sky.kr.co.newtogetusa.data.remote.dto

data class JoinResponse(
    val grantType : String,
    val accessToken : String,
    val accessTokenExpirationTime : Long,
    val refreshToken : String,
    val refreshTokenExpirationTime : Long
)