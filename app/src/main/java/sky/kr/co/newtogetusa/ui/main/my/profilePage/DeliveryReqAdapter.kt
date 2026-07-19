package sky.kr.co.newtogetusa.ui.main.my.profilePage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestBinding
import sky.kr.co.newtogetusa.utils.DeliveryStatusBadgeUtil
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage

class DeliveryReqAdapter(
    private val onItemClick: (DeliverySummaryDto) -> Unit
) : PagingDataAdapter<DeliverySummaryDto, DeliveryReqAdapter.ItemVH>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        return ItemVH(
            ItemDeliveryRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemVH, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    inner class ItemVH(private val binding: ItemDeliveryRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeliverySummaryDto) {
            item.setUiValue()
            binding.tvStatus.text = item.status_text
            DeliveryStatusBadgeUtil.apply(binding.tvStatus, item.status_cd)
            binding.tvDate.text = item.regist_date_text
            binding.tvTitle.text = item.title
            binding.tvPrice.text = item.price_text
            binding.tvPickupDate.text = item.pickup_ui_date
            binding.tvDepartAddress.text = item.depart_address
            binding.tvDestAddress.text = item.dest_address
            binding.ivProduct.loadImage(
                item.prd_picture,
                error = R.drawable.no_img,
                roundedCorner = 4.dpToPx()
            )
            binding.vRouteLine.isVisible = item.dest_address.isNotBlank()
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DeliverySummaryDto>() {
            override fun areItemsTheSame(
                oldItem: DeliverySummaryDto,
                newItem: DeliverySummaryDto
            ): Boolean {
                return oldItem.delivery_id == newItem.delivery_id
            }

            override fun areContentsTheSame(
                oldItem: DeliverySummaryDto,
                newItem: DeliverySummaryDto
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
