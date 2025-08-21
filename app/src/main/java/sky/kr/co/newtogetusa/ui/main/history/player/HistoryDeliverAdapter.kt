package sky.kr.co.newtogetusa.ui.main.history.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import sky.kr.co.newtogetusa.databinding.ItemHistoryBinding
import sky.kr.co.newtogetusa.databinding.ItemHistoryDeliveryBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class HistoryDeliverAdapter(private val viewModel: HistoryDeliveryViewModel) : PagingDataAdapter<String, BaseViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder((ItemHistoryDeliveryBinding.inflate(LayoutInflater.from(parent.context), parent, false)))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.onBindViewHolder(item, position)
        }
    }


    inner class ViewHolder(val binding: ItemHistoryDeliveryBinding) : BaseViewHolder(binding.root) {
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            binding.item = "aaa"
            binding.viewModel = viewModel
        }
    }

    companion object{
        private val diffCallback = object : DiffUtil.ItemCallback<String>(){
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}