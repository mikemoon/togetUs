package sky.kr.co.newtogetusa.ui.main.my.likeorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestBinding
import sky.kr.co.newtogetusa.utils.loadImage

class LikeOrderAdapter(
    private val onItemClick: (DeliverySummaryDto) -> Unit
) : PagingDataAdapter<DeliverySummaryDto, LikeOrderAdapter.VH>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemDeliveryRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    inner class VH(private val binding: ItemDeliveryRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeliverySummaryDto) {
            item.setUiValue()
            binding.tvStatus.text = item.status_text
            binding.tvDate.text = item.regist_date_text.ifBlank { item.regist_date.orEmpty() }
            binding.tvTitle.text = item.title
            binding.tvPrice.text = item.price_text
            binding.tvPickupDate.text = item.pickup_ui_date
            binding.tvDepartAddress.text = item.depart_address
            binding.tvDestAddress.text = item.dest_address
            binding.ivProduct.loadImage(item.prd_picture, error = R.drawable.no_img)
            binding.vRouteLine.isVisible = item.dest_address.isNotBlank()
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DeliverySummaryDto>() {
            override fun areItemsTheSame(oldItem: DeliverySummaryDto, newItem: DeliverySummaryDto): Boolean {
                return oldItem.delivery_id == newItem.delivery_id
            }

            override fun areContentsTheSame(oldItem: DeliverySummaryDto, newItem: DeliverySummaryDto): Boolean {
                return oldItem == newItem
            }
        }
    }
}
