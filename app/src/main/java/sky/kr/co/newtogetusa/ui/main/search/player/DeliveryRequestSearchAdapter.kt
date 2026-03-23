package sky.kr.co.newtogetusa.ui.main.search.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.users.DeliveryItem
import sky.kr.co.newtogetusa.databinding.ItemDeliveryRequestSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import java.text.DecimalFormat

class DeliveryRequestSearchAdapter :
    PagingDataAdapter<DeliveryItem, DeliveryRequestSearchAdapter.VH>(diff) {

    inner class VH(private val bind: ItemDeliveryRequestSearchBinding) : RecyclerView.ViewHolder(bind.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: DeliveryItem?) {
            if (item == null) return

            bind.tvStatus.text = item.status_cd
            bind.tvDate.text = item.regist_date.orEmpty()
            bind.tvContent.text = item.title
            bind.tvFee.text = "${DecimalFormat("#,###").format(item.fee_final)}원"

            bind.tvPickupType.text = if (item.pickup_immediately) "즉시 픽업" else "예약 픽업"
            bind.tvPickupDate.text = formatPickupDate(item.pickup_date)
            bind.tvImmediate.isVisible = item.pickup_immediately

            bind.tvDepartAddress.text = item.depart_address
            bind.tvDestAddress.text = item.dest_address
        }

        private fun formatPickupDate(raw: String): String {
            return if (raw.length >= 13) {
                "${raw.substring(0, 4)}.${raw.substring(4, 6)}.${raw.substring(6, 8)} ${raw.substring(9, 11)}:${raw.substring(11, 13)}"
            } else {
                raw
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemDeliveryRequestSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<DeliveryItem>() {
            override fun areItemsTheSame(oldItem: DeliveryItem, newItem: DeliveryItem): Boolean =
                oldItem.delivery_id == newItem.delivery_id

            override fun areContentsTheSame(oldItem: DeliveryItem, newItem: DeliveryItem): Boolean =
                oldItem == newItem
        }
    }
}
