package sky.kr.co.newtogetusa.ui.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ItemChatPlayerBinding
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.loadProfile

class ChattingPlayerAdapter(
    private val onItemClick: (ChatListItem) -> Unit
) : PagingDataAdapter<ChatListItem, ChattingPlayerAdapter.ViewHolder>(diff) {
    private val roomOverrides = mutableMapOf<Int, ChatRoomListUpdate>()

    inner class ViewHolder(private val binding: ItemChatPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatListItem?) {
            if (item == null) return
            val displayItem = item.withOverride()
            binding.item = displayItem
            if (displayItem.deliveryImageUrl.isNullOrBlank()) {
                binding.ivProduct.loadProfile(displayItem.profileUrl)
                binding.ivProfileBadge.isVisible = false
            } else {
                binding.ivProduct.loadImage(
                    displayItem.deliveryImageUrl,
                    placeholder = R.drawable.no_img,
                    error = R.drawable.no_img,
                    roundedCorner = 8.dpToPx()
                )
                binding.ivProfileBadge.isVisible = true
                binding.ivProfileBadge.loadProfile(displayItem.profileUrl)
            }
            binding.tvDate.isVisible = displayItem.date.isNotBlank()
            binding.tvMessage.isVisible = displayItem.message.isNotBlank()
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    fun applyRoomUpdate(update: ChatRoomListUpdate) {
        val roomId = update.roomId.toInt()
        val existing = roomOverrides[roomId]
        roomOverrides[roomId] = ChatRoomListUpdate(
            roomId = update.roomId,
            message = update.message ?: existing?.message,
            date = update.date ?: existing?.date
        )

        val index = snapshot().items.indexOfFirst { it.roomId == roomId }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    private fun ChatListItem.withOverride(): ChatListItem {
        val override = roomOverrides[roomId] ?: return this
        return copy(
            message = override.message ?: message,
            date = override.date ?: date
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatPlayerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<ChatListItem>() {
            override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean =
                oldItem == newItem
        }
    }
}
