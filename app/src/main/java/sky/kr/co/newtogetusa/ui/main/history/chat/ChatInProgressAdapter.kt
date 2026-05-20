package sky.kr.co.newtogetusa.ui.main.history.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ChatInProgressDto
import sky.kr.co.newtogetusa.databinding.ItemChatInProgressBinding
import sky.kr.co.newtogetusa.utils.loadProfile

class ChatInProgressAdapter(
    private val onClick: (ChatInProgressDto) -> Unit,
) : RecyclerView.Adapter<ChatInProgressAdapter.VH>() {
    private var items: List<ChatInProgressDto> = emptyList()

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
            ivProfile.loadProfile(item.playerProfile?.profileImage)
            tvName.text = item.playerProfile?.nickname.orEmpty()
            tvDate.text = item.lastMsg?.sendDate.orEmpty()
            tvMessage.text = item.lastMsg?.msg.orEmpty()
            tvUnread.text = when {
                (item.lastMsg?.unreadCnt ?: 0) > 99 -> "100+"
                else -> item.lastMsg?.unreadCnt?.toString().orEmpty()
            }
            tvUnread.visibility = if ((item.lastMsg?.unreadCnt ?: 0) > 0) android.view.View.VISIBLE else android.view.View.GONE
            root.setOnClickListener { onClick(item) }
        }
    }
}
