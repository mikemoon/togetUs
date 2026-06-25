package sky.kr.co.newtogetusa.ui.main.my.block

data class BlockedItem(
    val id: Int,
    val name: String,
    val profileImage: String?,
    val type: Type
) {
    enum class Type {
        USER,
        PLAYER
    }
}
