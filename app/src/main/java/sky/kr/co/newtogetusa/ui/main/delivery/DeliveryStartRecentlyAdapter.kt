package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemSearchRecentlyBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class DeliveryStartRecentlyAdapter: RecyclerView.Adapter<DeliveryStartRecentlyAdapter.ViewHolder>() {
    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchRecentlyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.onBindViewHolder(items[position], position)

    inner class ViewHolder(val binding: ItemSearchRecentlyBinding) : BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }
}