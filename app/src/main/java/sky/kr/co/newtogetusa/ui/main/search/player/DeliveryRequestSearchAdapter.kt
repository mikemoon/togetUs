package sky.kr.co.newtogetusa.ui.main.search.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class DeliveryRequestSearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<Any>()

    inner class VH(val bind:ItemDeliveryRequestSearchBinding): BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VH(ItemDeliveryRequestSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<Any>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = (holder as VH).onBindViewHolder(items[position], position)
}