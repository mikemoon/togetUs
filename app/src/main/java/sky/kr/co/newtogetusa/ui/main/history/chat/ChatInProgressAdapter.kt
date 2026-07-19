package sky.kr.co.newtogetusa.ui.main.history.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ChatInProgressDto
import sky.kr.co.newtogetusa.databinding.ItemChatInProgressBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatInProgressAdapter(
    private val onClick: (ChatInProgressDto) -> Unit,
    private val onSelect: (ChatInProgressDto) -> Unit,
    private val onReject: (ChatInProgressDto) -> Unit,
    private val onCancelSuggest: (ChatInProgressDto) -> Unit,
) : RecyclerView.Adapter<ChatInProgressAdapter.VH>() {
    private var items: List<ChatInProgressDto> = emptyList()
    var isSelectMode: Boolean = true

    fun submitItems(newItems: List<ChatInProgressDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemChatInProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val binding: ItemChatInProgressBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatInProgressDto) = with(binding) {
            if (item.prdPicture.isNullOrBlank()) {
                ivProfile.loadImage(
                    item.playerProfile?.profileImage,
                    placeholder = sky.kr.co.newtogetusa.R.drawable.profile,
                    error = sky.kr.co.newtogetusa.R.drawable.profile,
                    isCircle = true
                )
                ivBadge.isVisible = false
            } else {
                ivProfile.loadImage(
                    item.prdPicture,
                    placeholder = sky.kr.co.newtogetusa.R.drawable.no_img,
                    error = sky.kr.co.newtogetusa.R.drawable.no_img,
                    roundedCorner = 4.dpToPx()
                )
                ivBadge.isVisible = true
                ivBadge.loadImage(
                    item.playerProfile?.profileImage,
                    placeholder = sky.kr.co.newtogetusa.R.drawable.profile,
                    error = sky.kr.co.newtogetusa.R.drawable.profile,
                    isCircle = true
                )
            }
            tvName.text = item.playerProfile?.nickname.orEmpty()
            tvDate.text = item.lastMsg?.sendDate.toChatDisplayTime()
            tvMessage.text = item.lastMsg?.msg.orEmpty()
            tvUnread.text = when {
                (item.lastMsg?.unreadCnt ?: 0) > 99 -> "100+"
                else -> item.lastMsg?.unreadCnt?.toString().orEmpty()
            }
            tvUnread.visibility = if ((item.lastMsg?.unreadCnt ?: 0) > 0) android.view.View.VISIBLE else android.view.View.GONE
            val showApplyActions = isSelectMode && item.applyYn == "Y"
            val showSuggestCancel = isSelectMode && item.suggestYn == "Y"
            llActionButtons.isVisible = showApplyActions || showSuggestCancel
            btnReject.isVisible = showApplyActions
            btnSelect.isVisible = showApplyActions
            btnCancelSuggest.isVisible = showSuggestCancel
            root.setOnClickListener { onClick(item) }
            btnSelect.setOnClickListener { onSelect(item) }
            btnReject.setOnClickListener { onReject(item) }
            btnCancelSuggest.setOnClickListener { onCancelSuggest(item) }
        }
    }

    private fun String?.toChatDisplayTime(): String {
        val rawDate = this?.trim().orEmpty()
        if (rawDate.isBlank() || rawDate.startsWith("오전") || rawDate.startsWith("오후")) return rawDate

        val patterns = listOf(
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm",
            "yyyyMMdd HHmm"
        )
        val date = patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).parse(rawDate)
            }.getOrNull()
        } ?: return rawDate

        return SimpleDateFormat("a h:mm", Locale.KOREA).format(Date(date.time))
    }
}
