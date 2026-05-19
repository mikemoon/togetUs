package sky.kr.co.newtogetusa.ui.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemChatPlayerBinding
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem
import sky.kr.co.newtogetusa.utils.loadProfile

class ChattingPlayerAdapter(
    private val onItemClick: (ChatListItem) -> Unit
) : PagingDataAdapter<ChatListItem, ChattingPlayerAdapter.ViewHolder>(diff) {

    inner class ViewHolder(private val binding: ItemChatPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatListItem?) {
            if (item == null) return
            binding.item = item
            binding.ivProfile.loadProfile(item.profileUrl)
            binding.root.setOnClickListener { onItemClick(item) }
        }
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
