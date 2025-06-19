package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemSearchResultBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class DeliveryStartSearchResultAdapter(private val viewModel: DeliveryStartViewModel) : RecyclerView.Adapter<DeliveryStartSearchResultAdapter.ViewHolder>() {

    val items =  mutableListOf<SearchResultModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindViewHolder(items[position], position)
    }

    fun updateList(newList: List<SearchResultModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemSearchResultBinding) : BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            binding.tvSearchAddress.text = (data as SearchResultModel).placeName
            binding.root.setOnClickListener {
                viewModel.onAddressClick(data)
            }
        }
    }
}