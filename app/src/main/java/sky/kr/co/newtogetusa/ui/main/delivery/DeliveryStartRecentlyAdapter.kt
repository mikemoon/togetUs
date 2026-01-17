package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemSearchRecentlyBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class DeliveryStartRecentlyAdapter(
    private val onClick: (String) -> Unit,
    private val onRemove: (String) -> Unit
): RecyclerView.Adapter<DeliveryStartRecentlyAdapter.ViewHolder>() {
    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchRecentlyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.onBindViewHolder(items[position], position)

    inner class ViewHolder(val b: ItemSearchRecentlyBinding) : BaseViewHolder(b.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)

            b.tvWord.text = data as String
            b.ivDele.setOnClickListener {
                onRemove(data)
            }
            b.root.setOnClickListener {
                onClick(data)
            }
        }
    }
}