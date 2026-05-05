package sky.kr.co.newtogetusa.ui.main.history.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemHistoryDeliveryCalendarDummyBinding

class HistoryDeliveryCalendarDummyAdapter :
    RecyclerView.Adapter<HistoryDeliveryCalendarDummyAdapter.ViewHolder>() {

    private val items = mutableListOf<HistoryDeliveryCalendarDummyItem>()

    fun submitList(newItems: List<HistoryDeliveryCalendarDummyItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryDeliveryCalendarDummyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemHistoryDeliveryCalendarDummyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryDeliveryCalendarDummyItem) {
            binding.tvStatus.text = item.status
            binding.tvDate.text = item.date
            binding.tvTitle.text = item.title
            binding.tvPrice.text = item.price
            binding.tvPickupDate.text = item.pickupDate
            binding.tvPickupAddress.text = item.pickupAddress
            binding.tvArrivalAddress.text = item.arrivalAddress
        }
    }
}

data class HistoryDeliveryCalendarDummyItem(
    val status: String,
    val date: String,
    val title: String,
    val price: String,
    val pickupDate: String,
    val pickupAddress: String,
    val arrivalAddress: String
)
