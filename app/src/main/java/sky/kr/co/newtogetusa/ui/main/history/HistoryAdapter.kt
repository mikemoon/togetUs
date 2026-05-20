package sky.kr.co.newtogetusa.ui.main.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.toast

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

            updateBottomButtons(item)
        }

        private fun updateBottomButtons(item: DeliverySummaryDto) = with(binding) {
            fun setDualButtons(
                secondaryText: String,
                primaryText: String,
                secondaryAction: (() -> Unit)? = null,
                primaryAction: (() -> Unit)? = null
            ) {
                llBottomButtons.visibility = View.VISIBLE

                tvSecondaryButton.apply {
                    visibility = View.VISIBLE
                    text = secondaryText
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 1f
                        marginEnd = 4.dpToPx()
                    }
                    setOnClickListener { secondaryAction?.invoke() }
                }

                tvPrimaryButton.apply {
                    visibility = View.VISIBLE
                    text = primaryText
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 1f
                        marginStart = 4.dpToPx()
                    }
                    setOnClickListener { primaryAction?.invoke() }
                }
            }

            fun setSinglePrimaryButton(
                primaryText: String,
                primaryAction: (() -> Unit)? = null
            ) {
                llBottomButtons.visibility = View.VISIBLE

                tvSecondaryButton.visibility = View.GONE

                tvPrimaryButton.apply {
                    visibility = View.VISIBLE
                    text = primaryText
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        weight = 0f
                        marginStart = 0
                    }
                    setOnClickListener { primaryAction?.invoke() }
                }
            }

            fun hideButtons() {
                llBottomButtons.visibility = View.GONE
            }

            val openChat = {
                this@HistoryAdapter.viewModel.onMenuBottonClick(HistoryViewModel.MenuButton.MenuChat)
            }
            val pending = { root.context.toast("준비중인 기능입니다.") }
            val modifyPending: (DeliverySummaryDto) -> Unit = { item ->
                this@HistoryAdapter.viewModel.onMenuBottonClick(
                    HistoryViewModel.MenuButton.MenuModify(item)
                )
            }
            val openDeliveryStatus: (DeliverySummaryDto) -> Unit = { item ->
                this@HistoryAdapter.viewModel.onMenuBottonClick(
                    HistoryViewModel.MenuButton.MenuDeliveryStatus(item)
                )
            }

            when {
                item.status_cd == "REGISTER_ING" -> setDualButtons(
                    secondaryText = "삭제하기",
                    primaryText = "수정하기",
                    secondaryAction = { this@HistoryAdapter.viewModel.onItemCancel(item) },
                    primaryAction = { modifyPending(item) }
                )

                item.status_cd == "MATCH_BEFORE" -> setDualButtons(
                    secondaryText = "취소하기",
                    primaryText = "수정하기",
                    secondaryAction = { this@HistoryAdapter.viewModel.onItemCancel(item) },
                    primaryAction = { modifyPending(item) }
                )

                item.status_cd.startsWith("MATCH") -> setDualButtons(
                    secondaryText = "취소하기",
                    primaryText = "채팅확인",
                    secondaryAction = { this@HistoryAdapter.viewModel.onItemCancel(item) },
                    primaryAction = openChat
                )

                item.status_cd == "DELIVERY_BEFORE" || item.status_cd == "DELIVERY_WAIT" -> setSinglePrimaryButton(
                    primaryText = "채팅하기",
                    primaryAction = openChat
                )

                item.status_cd == "DELIVERY_START" || item.status_cd == "PICKUP_START" || item.status_cd == "DELIVERY_DEPART" -> setDualButtons(
                    secondaryText = "채팅하기",
                    primaryText = "픽업확인",
                    secondaryAction = openChat,
                    primaryAction = pending
                )

                item.status_cd == "DELIVERY_ING" || item.status_cd.startsWith("ING") -> setDualButtons(
                    secondaryText = "채팅하기",
                    primaryText = "동행현황",
                    secondaryAction = openChat,
                    primaryAction = { openDeliveryStatus(item) }
                )

                item.status_cd.startsWith("DONE") -> setSinglePrimaryButton(
                    primaryText = "수령확인",
                    primaryAction = pending
                )

                item.status_cd.startsWith("CANCEL") -> hideButtons()

                else -> hideButtons()
            }
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
