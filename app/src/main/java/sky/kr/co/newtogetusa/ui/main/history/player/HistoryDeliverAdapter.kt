package sky.kr.co.newtogetusa.ui.main.history.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.player.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemHistoryDeliveryBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.utils.DeliveryStatusBadgeUtil
import sky.kr.co.newtogetusa.utils.TextConvertUtil.toWon
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.formatPickupDateTime
import sky.kr.co.newtogetusa.utils.loadImage

class HistoryDeliverAdapter(
    private val viewModel: HistoryDeliveryViewModel,
    private val onItemClick: (DeliverySummaryDto) -> Unit
) : PagingDataAdapter<DeliverySummaryDto, BaseViewHolder>(diffCallback) {

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
            val item = data as DeliverySummaryDto
            binding.item = item
            binding.viewModel = viewModel
            binding.tvTitle.text = item.title
            binding.tvPrice.text = item.feeFinal.toWon()
            binding.tvPickupDate.text = formatPickupDateTime(item.pickupDate)
            DeliveryStatusBadgeUtil.apply(binding.tvStatus, item.statusCd)
            binding.ivProduct.loadImage(
                item.prdPicture,
                placeholder = R.drawable.no_img,
                error = R.drawable.no_img,
                roundedCorner = 4.dpToPx()
            )
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object{
        private val diffCallback = object : DiffUtil.ItemCallback<DeliverySummaryDto>(){
            override fun areItemsTheSame(oldItem: DeliverySummaryDto, newItem: DeliverySummaryDto): Boolean {
                return oldItem.deliveryId == newItem.deliveryId
            }

            override fun areContentsTheSame(oldItem: DeliverySummaryDto, newItem: DeliverySummaryDto): Boolean {
                return oldItem == newItem
            }
        }
    }
}
