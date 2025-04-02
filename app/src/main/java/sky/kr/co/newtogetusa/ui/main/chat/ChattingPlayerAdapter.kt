package sky.kr.co.newtogetusa.ui.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemChatPlayerBinding
import sky.kr.co.newtogetusa.databinding.ItemChatPlayerEmptyBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem

class ChattingPlayerAdapter(private val viewModel: ChattingPlayerViewModel) :
    RecyclerView.Adapter<BaseViewHolder>() {
    var items = mutableListOf<ChatListItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (items.isEmpty()) {
            EmptyViewHolder(
                ItemChatPlayerEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ViewHolder(
                ItemChatPlayerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 1 else items.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int){
        if(items.isEmpty()){
            holder.onBindViewHolder(null, position)
        }else{
            holder.onBindViewHolder(items[position], position)
        }
    }

    inner class ViewHolder(private val binding: ItemChatPlayerBinding) :
        BaseViewHolder(binding.root) {
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            if (data !is ChatListItem) return
            binding.item = data
            binding.viewModel = viewModel
        }
    }

    inner class EmptyViewHolder(private val binding: ItemChatPlayerEmptyBinding) :
        BaseViewHolder(binding.root) {
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }
}