package sky.kr.co.newtogetusa.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.utils.loadImage

class HistoryAdapter(private val viewModel: HistoryViewModel) : PagingDataAdapter<DeliverySummaryDto, BaseViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder((ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.onBindViewHolder(item, position)
        }
    }


    inner class ViewHolder(val binding: ItemHistoryBinding) : BaseViewHolder(binding.root) {
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            val item = data as DeliverySummaryDto
            item.setStatusText()

            binding.item = item
            binding.viewModel = viewModel
            binding.ivProduct.loadImage(item.prd_picture, error = R.drawable.no_img)
        }
    }

    companion object{
        private val diffCallback = object : DiffUtil.ItemCallback<DeliverySummaryDto>(){
            override fun areItemsTheSame(oldItem: DeliverySummaryDto, newItem: DeliverySummaryDto): Boolean {
                return oldItem.delivery_id == newItem.delivery_id
            }

            override fun areContentsTheSame(oldItem: DeliverySummaryDto, newItem: DeliverySummaryDto): Boolean {
                return oldItem == newItem
            }
        }
    }
}