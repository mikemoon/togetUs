package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ItemBottomSuggestDeliveryBinding

class BottomSuggestDeliveryAdapter(
    private val onItemClick: (BottomSuggestDeliveryDialog.SuggestRequestItem) -> Unit
) : RecyclerView.Adapter<BottomSuggestDeliveryAdapter.VH>() {

    private var items: List<BottomSuggestDeliveryDialog.SuggestRequestItem> = emptyList()
    private var selectedIndex: Int = 0

    fun submitList(
        items: List<BottomSuggestDeliveryDialog.SuggestRequestItem>,
        selectedIndex: Int = 0
    ) {
        this.items = items
        this.selectedIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemBottomSuggestDeliveryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class VH(private val binding: ItemBottomSuggestDeliveryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BottomSuggestDeliveryDialog.SuggestRequestItem, isSelected: Boolean) {
            binding.tvTitle.text = item.title
            binding.tvRoute.text = item.routeText
            binding.ivRadio.setImageResource(if (isSelected) R.drawable.radio_selected else R.drawable.radio)
            binding.root.setOnClickListener {
                val oldIndex = selectedIndex
                selectedIndex = bindingAdapterPosition
                if (oldIndex != selectedIndex) {
                    notifyItemChanged(oldIndex)
                    notifyItemChanged(selectedIndex)
                }
                items.getOrNull(selectedIndex)?.let(onItemClick)
            }
        }
    }
}
