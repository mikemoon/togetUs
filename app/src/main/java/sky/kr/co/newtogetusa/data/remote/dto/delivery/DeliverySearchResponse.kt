package sky.kr.co.newtogetusa.data.remote.dto.delivery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import sky.kr.co.newtogetusa.utils.TextConvertUtil.toWon
import sky.kr.co.newtogetusa.utils.formatPickupDateTime

data class DeliverySearchResponse(
    val page_no: Int,
    val total_cnt: Int,
    val has_more: Boolean,
    val deliveries: List<DeliverySummaryDto>
)

@Parcelize
data class DeliverySummaryDto(
    val delivery_id: Long,
    val requester_id: Long,
    val player_id: Long?,          // null 가능
    val title: String,
    val status_cd: String,
    val prd_picture: String?,      // null 가능
    val depart_address: String,
    val dest_address: String,
    val pickup_immediately: Boolean,
    val pickup_date: String,       // "20250912 1430"
    val fee_final: Int,
    val regist_date: String?,      // null 가능
    val apply_date: String? ,       // null

    var status_text: String,
    var price_text: String,
    var pickup_ui_date: String,
): Parcelable {

    fun setUiValue(){
        setStatusText()
        price_text = fee_final.toWon()
        pickup_ui_date = formatPickupDateTime(pickup_date)
    }
    fun setStatusText(){
        status_text = when(status_cd){
            "REGISTER_ING" -> "작성중"
            "MATCH_BEFORE" -> "매칭대기중"
            "CANCEL" -> "취소완료"
            else -> "동행시작"
        }
    }

}