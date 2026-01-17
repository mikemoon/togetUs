package sky.kr.co.newtogetusa.ui.main.my.profilePage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestBinding
import sky.kr.co.newtogetusa.databinding.ItemProfileDeliveryReqTopBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class DeliveryReqAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return when(getItemViewType(position)){
            VH_TOP ->{
                TopVH(ItemProfileDeliveryReqTopBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else ->{
                ItemVH(ItemDeliveryRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(position){
            0 ->{
                (vh as TopVH).onBindViewHolder("", position)
            }
            else ->{
                (vh as ItemVH).onBindViewHolder(items[position-1], position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0){
            VH_TOP
        }else{
            VH_ITEM
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(listItem: List<String>){
        items.clear()
        items.addAll(listItem)
        notifyDataSetChanged()
    }

    inner class TopVH(private val binding: ItemProfileDeliveryReqTopBinding): BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }

    inner class ItemVH(private val binding:ItemDeliveryRequestBinding): BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }

    companion object{
        private const val VH_TOP = 0
        private const val VH_ITEM = 1
    }
}