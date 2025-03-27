package sky.kr.co.newtogetusa.ui.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemChatPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class ChattingPlayerAdapter :RecyclerView.Adapter<BaseViewHolder>(){
    var items = mutableListOf<Any>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder(ItemChatPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.onBindViewHolder(items[position], position)


    inner class ViewHolder(private val binding: ItemChatPlayerBinding) : BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }
}