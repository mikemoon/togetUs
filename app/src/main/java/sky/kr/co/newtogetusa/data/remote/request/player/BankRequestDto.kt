package sky.kr.co.newtogetusa.data.remote.request.player

data class BankRequestDto(
    val term_cds: List<String>,
    val bank_cd: String,
    val account_number: String,
    val account_depositor: String
)