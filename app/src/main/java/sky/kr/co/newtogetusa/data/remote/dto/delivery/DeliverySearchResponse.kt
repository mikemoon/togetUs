package sky.kr.co.newtogetusa.data.remote.dto.delivery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import sky.kr.co.newtogetusa.utils.TextConvertUtil.toWon
import sky.kr.co.newtogetusa.utils.formatPickupDateTime
import sky.kr.co.newtogetusa.utils.formatRegisterDateTime

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
    var regist_date_text: String,
): Parcelable {

    fun setUiValue(){
        setStatusText()
        price_text = fee_final.toWon()
        pickup_ui_date = formatPickupDateTime((pickup_date as String?).orEmpty())
        regist_date_text = formatRegisterDateTime((regist_date as String?).orEmpty())
    }
    fun setStatusText(){
        status_text = when(status_cd as String){
            "REGISTER_ING" -> "작성중"
            "MATCH_BEFORE" -> "매칭대기중"
            "CANCEL" -> "취소완료"
            else -> "동행시작"
        }
    }

    override fun hashCode(): Int {
        var result = delivery_id.hashCode()
        result = 31 * result + requester_id.hashCode()
        result = 31 * result + player_id.hashCode()
        result = 31 * result + (title as String?).hashCode()
        result = 31 * result + (status_cd as String?).hashCode()
        result = 31 * result + prd_picture.hashCode()
        result = 31 * result + (depart_address as String?).hashCode()
        result = 31 * result + (dest_address as String?).hashCode()
        result = 31 * result + pickup_immediately.hashCode()
        result = 31 * result + (pickup_date as String?).hashCode()
        result = 31 * result + fee_final
        result = 31 * result + regist_date.hashCode()
        result = 31 * result + apply_date.hashCode()
        result = 31 * result + (status_text as String?).hashCode()
        result = 31 * result + (price_text as String?).hashCode()
        result = 31 * result + (pickup_ui_date as String?).hashCode()
        return result
    }

}