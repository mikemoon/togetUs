package sky.kr.co.newtogetusa.data.remote.dto.chat

data class ChatRoomSearchResponseDto(
    val page_no: Int,
    val total_cnt: Int,
    val has_more: Boolean,
    val unread_cnt: Int,
    val rooms: List<ChatRoomSearchRoomDto>
)

data class ChatRoomSearchRoomDto(
    val room_id: Int,
    val room_name: String,
    val read_msg_id: Int,
    val unread_cnt: Int,
    val delivery: ChatRoomSearchDeliveryDto,
    val last_msg: ChatRoomLastMessageDto?,
    val player: ChatRoomParticipantDto?,
    val user: ChatRoomParticipantDto?,
    val is_cancel: Boolean,
    val is_blocked: Boolean,
    val is_reported: Boolean
) {
    val isDisabled: Boolean
        get() = delivery.status_cd in DISABLED_DELIVERY_STATUSES ||
            is_cancel || is_blocked || is_reported

    private companion object {
        val DISABLED_DELIVERY_STATUSES = setOf("DELIVERY_END", "CANCEL")
    }
}

data class ChatRoomSearchDeliveryDto(
    val delivery_id: Int,
    val status_cd: String,
    val prd_picture: String? = null
)

data class ChatRoomParticipantDto(
    val player_id: Int?,
    val user_id: Int,
    val nickname: String,
    val profile_image: String?,
    val enable: Boolean
)
