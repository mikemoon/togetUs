package sky.kr.co.newtogetusa.ui.main.search.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.users.DeliveryItem
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestSearchBinding
import sky.kr.co.newtogetusa.utils.DeliveryStatusBadgeUtil
import sky.kr.co.newtogetusa.utils.loadImage
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class DeliveryRequestSearchAdapter(
    private val onItemClick: (DeliveryItem) -> Unit
) :
    PagingDataAdapter<DeliveryItem, DeliveryRequestSearchAdapter.VH>(diff) {

    inner class VH(private val bind: ItemDeliveryRequestSearchBinding) : RecyclerView.ViewHolder(bind.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: DeliveryItem?) {
            if (item == null) return

            bind.tvStatus.text = formatStatus(item.status_cd)
            DeliveryStatusBadgeUtil.apply(bind.tvStatus, item.status_cd)
            bind.tvDate.text = formatRegistDate(item.regist_date)
            bind.tvContent.text = item.title
            bind.tvFee.text = "${DecimalFormat("#,###").format(item.fee_final)}원"
            bind.ivPic.loadImage(item.prd_picture, roundedCorner = 4, error = R.drawable.no_img)

            //bind.tvPickupType.text = if (item.pickup_immediately) "즉시 픽업" else "예약 픽업"
            bind.tvPickupDate.text = formatPickupDate(item.pickup_date, item.pickup_time)
            bind.tvImmediate.isVisible = item.pickup_immediately

            bind.tvDepartAddress.text = item.depart_address
            bind.tvDestAddress.text = item.dest_address
            bind.root.setOnClickListener { onItemClick(item) }
        }

        private fun formatStatus(statusCd: String): String =
            when (statusCd) {
                "REGISTER_ING" -> "작성중"
                "MATCH_BEFORE" -> "매칭 대기중"
                "MATCH_ING" -> "매칭 진행중"
                "DELIVERY_BEFORE" -> "동행 대기중"
                "DELIVERY_START" -> "동행 시작"
                "DELIVERY_ING" -> "동행중"
                "DELIVERY_END" -> "동행 완료"
                "CANCEL" -> "취소완료"
                else -> statusCd
            }

        private fun formatRegistDate(registDate: String?): String {
            val datePart = registDate.orEmpty().substringBefore(" ")
            val patterns = listOf("yyyyMMdd", "yyyy-MM-dd", "yyyy.MM.dd")

            patterns.forEach { pattern ->
                runCatching {
                    return LocalDate.parse(datePart, DateTimeFormatter.ofPattern(pattern))
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                }
            }

            return datePart
        }

        private fun formatPickupDate(pickupDate: String, pickupTime: String?): String {
            val parts = pickupDate.trim().split(Regex("\\s+"))
            val datePart = parts.getOrNull(0).orEmpty()
            val timePart = pickupTime?.takeIf { it.isNotBlank() }
                ?: parts.getOrNull(1).orEmpty()

            if (datePart.length != 8 || timePart.isBlank() || timePart.equals("null", ignoreCase = true)) {
                return pickupDate
            }

            return runCatching {
                val date = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyyMMdd"))
                val time = LocalTime.parse(timePart.padStart(4, '0'), DateTimeFormatter.ofPattern("HHmm"))
                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                val amPm = if (time.hour < 12) "오전" else "오후"
                val hour12 = when {
                    time.hour == 0 -> 12
                    time.hour > 12 -> time.hour - 12
                    else -> time.hour
                }

                "${date.year}년 ${date.monthValue}월${date.dayOfMonth}일($dayOfWeek) $amPm $hour12:${"%02d".format(time.minute)}"
            }.getOrElse { pickupDate }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemDeliveryRequestSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<DeliveryItem>() {
            override fun areItemsTheSame(oldItem: DeliveryItem, newItem: DeliveryItem): Boolean =
                oldItem.delivery_id == newItem.delivery_id

            override fun areContentsTheSame(oldItem: DeliveryItem, newItem: DeliveryItem): Boolean =
                oldItem == newItem
        }
    }
}
