package sky.kr.co.newtogetusa.ui.main.my.profilePage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestBinding
import sky.kr.co.newtogetusa.databinding.ItemProfileDeliveryReqTopBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.ui.main.my.ProfileManagementViewModel
import sky.kr.co.newtogetusa.utils.DeliveryStatusBadgeUtil
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage

class DeliveryReqAdapter(
    private val viewModel: ProfileManagementViewModel,
    private val onItemClick: (DeliverySummaryDto) -> Unit
) : PagingDataAdapter<DeliverySummaryDto, RecyclerView.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VH_TOP -> TopVH(
                ItemProfileDeliveryReqTopBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> ItemVH(
                ItemDeliveryRequestBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TopVH -> holder.bind()
            is ItemVH -> getItem(position - 1)?.let { holder.bind(it) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VH_TOP else VH_ITEM
    }

    inner class TopVH(private val binding: ItemProfileDeliveryReqTopBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvTopAll.isSelected =
                viewModel.topMenuLiveData.value is ProfileManagementViewModel.TopMenu.All
            binding.tvTopDoing.isSelected =
                viewModel.topMenuLiveData.value is ProfileManagementViewModel.TopMenu.Doing
            binding.tvTopEnd.isSelected =
                viewModel.topMenuLiveData.value is ProfileManagementViewModel.TopMenu.End
            binding.tvTopAll.setOnClickListener { viewModel.onTopMenuSelect(viewModel.menuAll) }
            binding.tvTopDoing.setOnClickListener { viewModel.onTopMenuSelect(viewModel.menuDoing) }
            binding.tvTopEnd.setOnClickListener { viewModel.onTopMenuSelect(viewModel.menuEnd) }
        }
    }

    inner class ItemVH(private val binding: ItemDeliveryRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeliverySummaryDto) {
            item.setUiValue()
            binding.tvStatus.text = item.status_text
            DeliveryStatusBadgeUtil.apply(binding.tvStatus, item.status_cd)
            binding.tvDate.text = item.regist_date.orEmpty()
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
        private const val VH_TOP = 0
        private const val VH_ITEM = 1

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
