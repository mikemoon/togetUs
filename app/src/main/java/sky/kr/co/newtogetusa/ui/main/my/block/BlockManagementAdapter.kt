package sky.kr.co.newtogetusa.ui.main.my.block

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemBlockedUserBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class BlockManagementAdapter(
    private val onUnblock: (BlockedItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<BlockedItem>()

    fun setItems(data: List<BlockedItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemBlockedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(items[position])
    }

    inner class ViewHolder(
        private val binding: ItemBlockedUserBinding
    ) : BaseViewHolder(binding.root) {

        fun bind(item: BlockedItem) {
            binding.item = item
            binding.tvType.text = when (item.type) {
                BlockedItem.Type.USER -> "유저"
                BlockedItem.Type.PLAYER -> "플레이어"
            }
            binding.btnUnblock.setOnClickListener { onUnblock(item) }
            binding.executePendingBindings()
        }
    }
}
