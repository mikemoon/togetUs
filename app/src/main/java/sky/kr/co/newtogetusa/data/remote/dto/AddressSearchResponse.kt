package sky.kr.co.newtogetusa.data.remote.dto

data class AddressSearchResponse(
    val meta: Meta,
    val documents: List<Document>
){
    data class Meta(
        val total_count: Int,
        val pageable_count: Int,
        val is_end: Boolean
    )
    data class Document(
        val address_name: String,
        val address_type: String,
        val x: String,
        val y: String
    )
}
