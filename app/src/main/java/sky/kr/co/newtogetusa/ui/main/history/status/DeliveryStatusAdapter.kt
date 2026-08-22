package sky.kr.co.newtogetusa.ui.main.history.status

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryStatusLogDto
import sky.kr.co.newtogetusa.databinding.ItemDeliveryStatusLogBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

class DeliveryStatusAdapter : RecyclerView.Adapter<DeliveryStatusAdapter.VH>() {

    private var items: List<DeliveryStatusLogDto> = emptyList()

    private companion object {
        val DISPLAY_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy년 M월d일(E) a h:mm", Locale.KOREAN)
        val DATE_FORMATS = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .toFormatter(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyyMMdd HHmm"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        )
    }

    fun submitItems(newItems: List<DeliveryStatusLogDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemDeliveryStatusLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position == 0)
    }

    override fun getItemCount(): Int = items.size

    inner class VH(private val binding: ItemDeliveryStatusLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeliveryStatusLogDto, isFirst: Boolean) = with(binding) {
            tvStatus.text = statusTitle(item)
            tvDate.text = formatRegDate(item.regDate)
            tvActor.isVisible = false
            vTopLine.isVisible = isFirst

            val pictures = item.pictures.orEmpty()
            val imageViews = listOf(ivPhoto1, ivPhoto2, ivPhoto3)
            imageViews.forEachIndexed { index, imageView ->
                val url = pictures.getOrNull(index)
                imageView.isVisible = url != null
                imageView.loadImage(url, roundedCorner = 8.dpToPx(), error = R.drawable.no_img)
            }
            tvMoreCount.isVisible = pictures.size > imageViews.size
            tvMoreCount.text = "+${pictures.size - imageViews.size}"
            llPictures.isVisible = pictures.isNotEmpty()
        }

        private fun statusTitle(item: DeliveryStatusLogDto): String {
            // 서버 status_name이 영문으로 내려오는 경우에도 iOS와 동일하게 코드 기준으로 표시한다.
            val codeTitle = when (item.statusCd.orEmpty()) {
                "REGISTER_ING" -> "작성중"
                "MATCH_BEFORE" -> "매칭 대기중"
                "MATCH_ING" -> "매칭 진행중"
                "DELIVERY_BEFORE", "DELIVERY_WAIT" -> "동행 대기중"
                "DELIVERY_START", "PICKUP_START", "DELIVERY_DEPART" -> "동행 시작"
                "DELIVERY_ING", "ING", "ING_START", "ING_DELIVERY" -> "동행중"
                "DELIVERY_END", "DONE", "DONE_DELIVERY" -> "동행 완료"
                "CANCEL", "CANCEL_DONE" -> "취소완료"
                else -> ""
            }
            return codeTitle.ifBlank {
                item.statusName.orEmpty().ifBlank { item.statusCd.orEmpty() }
            }
        }

        private fun formatRegDate(raw: String?): String {
            val value = raw?.trim().orEmpty()
            if (value.isBlank()) return ""

            val normalized = value.replace('T', ' ')
            val dateTime = DATE_FORMATS.firstNotNullOfOrNull { formatter ->
                try {
                    LocalDateTime.parse(value, formatter)
                } catch (_: DateTimeParseException) {
                    try {
                        LocalDateTime.parse(normalized, formatter)
                    } catch (_: DateTimeParseException) {
                        null
                    }
                }
            } ?: runCatching { OffsetDateTime.parse(value).toLocalDateTime() }.getOrNull()
                ?: return value

            return dateTime.format(DISPLAY_FORMAT)
        }

    }
}
