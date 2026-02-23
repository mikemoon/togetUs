package sky.kr.co.newtogetusa.data.remote.request.player

data class PlayerJoinMultipartData(
    val bank: BankRequest,
    val introduction: String,
    val areas: List<AreaRequest>
)