package sky.kr.co.newtogetusa.data.remote.dto.chat

data class Room(
    val reg_date : String,
    val chg_date : String,
    val room_id : Int,
    val last_msg_id : Int,
    val last_user_id : Int,
    val last_msg : String,
    val last_date : String,
    val read_msg_id : Int,
    val unread_cnt : Int,
    val delivery_id : Int,
    val delivery_status_cd: String
)
