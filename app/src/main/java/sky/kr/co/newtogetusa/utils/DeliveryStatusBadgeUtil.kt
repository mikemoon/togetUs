package sky.kr.co.newtogetusa.utils

import android.widget.TextView
import androidx.core.content.ContextCompat
import sky.kr.co.newtogetusa.R

object DeliveryStatusBadgeUtil {
    fun apply(textView: TextView, statusCd: String?) {
        val code = statusCd.orEmpty()
        val isActiveDelivery = when (code) {
            "DELIVERY_START",
            "PICKUP_START",
            "DELIVERY_DEPART",
            "DELIVERY_ING",
            "ING",
            "ING_START",
            "ING_DELIVERY",
            "DELIVERY_END",
            "DONE",
            "DONE_END",
            "DONE_DELIVERY" -> true
            else -> false
        }

        textView.text = titleOf(code)

        val textColor = if (isActiveDelivery) R.color.primary_100 else R.color.black_60
        val background = if (isActiveDelivery) {
            R.drawable.background_s_primary5_r4
        } else {
            R.drawable.background_s_b5_r4
        }

        textView.setTextColor(ContextCompat.getColor(textView.context, textColor))
        textView.background = ContextCompat.getDrawable(textView.context, background)
    }

    fun titleOf(statusCd: String?): String =
        when (statusCd.orEmpty()) {
            "REGISTER_ING" -> "작성중"
            "MATCH_BEFORE" -> "매칭 대기중"
            "MATCH_ING" -> "매칭 진행중"
            "DELIVERY_BEFORE" -> "동행 대기중"
            "DELIVERY_START" -> "동행 시작"
            "PICKUP_START" -> "동행 시작"
            "DELIVERY_DEPART" -> "동행 시작"
            "DELIVERY_WAIT" -> "동행 대기중"
            "DELIVERY_ING" -> "동행중"
            "ING" -> "동행중"
            "ING_START" -> "동행중"
            "ING_DELIVERY" -> "동행중"
            "DELIVERY_END" -> "동행 완료"
            "DONE" -> "동행 완료"
            "DONE_END" -> "동행 완료"
            "DONE_DELIVERY" -> "동행 완료"
            "CANCEL" -> "취소완료"
            "CANCEL_DONE" -> "취소완료"
            else -> statusCd.orEmpty()
        }
}
