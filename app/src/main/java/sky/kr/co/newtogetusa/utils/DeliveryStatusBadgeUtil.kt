package sky.kr.co.newtogetusa.utils

import android.widget.TextView
import androidx.core.content.ContextCompat
import sky.kr.co.newtogetusa.R

object DeliveryStatusBadgeUtil {
    fun apply(textView: TextView, statusCd: String?) {
        val isActiveDelivery = when (statusCd.orEmpty()) {
            "DELIVERY_START",
            "PICKUP_START",
            "DELIVERY_DEPART",
            "DELIVERY_ING",
            "ING",
            "ING_START",
            "ING_DELIVERY" -> true
            else -> false
        }

        val textColor = if (isActiveDelivery) R.color.primary_100 else R.color.black_60
        val background = if (isActiveDelivery) {
            R.drawable.background_s_primary5_r4
        } else {
            R.drawable.background_s_b5_r4
        }

        textView.setTextColor(ContextCompat.getColor(textView.context, textColor))
        textView.background = ContextCompat.getDrawable(textView.context, background)
    }
}

