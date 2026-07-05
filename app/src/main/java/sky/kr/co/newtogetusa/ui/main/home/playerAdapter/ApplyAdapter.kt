package sky.kr.co.newtogetusa.ui.main.home.playerAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemHomeBottomButtonBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeContentsBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeEmptyBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeTitleBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage

class ApplyAdapter(
    private val onItemClickListener: ((DeliverySummaryDto) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<DeliverySummaryDto>()
    private var hasMore = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_TITLE -> TitleVH(ItemHomeTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_EMPTY -> EmptyVH(ItemHomeEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_BOTTOM_BUTTON -> BottomButtonVH(
                ItemHomeBottomButtonBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            else -> ContentsVH(ItemHomeContentsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return when {
            items.isEmpty() -> 2
            hasMore -> items.size + 2
            else -> items.size + 1
        }
    }

    fun setItems(items: List<DeliverySummaryDto>, hasMore: Boolean = false){
        this.items.clear()
        this.items.addAll(items)
        this.hasMore = hasMore
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) VIEW_TYPE_TITLE else{
            if(items.size == 0){
                VIEW_TYPE_EMPTY
            }else{
                if(hasMore && position == items.size + 1) VIEW_TYPE_BOTTOM_BUTTON else
                    VIEW_TYPE_CONTENTS
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            VIEW_TYPE_TITLE -> (holder as TitleVH).bind("")
            VIEW_TYPE_EMPTY -> (holder as EmptyVH).bind("")
            VIEW_TYPE_BOTTOM_BUTTON -> (holder as BottomButtonVH).bind("")
            else -> (holder as ContentsVH).bind(items[position-1])
        }
    }

    inner class TitleVH(private val binding: ItemHomeTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.tvTitle.text = "지원한 동행"
        }
    }

    inner class ContentsVH(private val binding: ItemHomeContentsBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: DeliverySummaryDto) {
            item.setStatusText()
            binding.data = item
            bindAddress(item)
            binding.ivProduct.loadImage(
                item.prd_picture,
                placeholder = R.drawable.no_img,
                error = R.drawable.no_img,
                roundedCorner = 4.dpToPx()
            )
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }

        private fun bindAddress(item: DeliverySummaryDto) {
            val showDestination = item.status_cd in deliveryStartedStatuses
            binding.tvTarget.text = if (showDestination) "도착지" else "픽업지"
            binding.tvAddress.text = if (showDestination) item.dest_address else item.depart_address
        }
    }

    inner class BottomButtonVH(private val binding: ItemHomeBottomButtonBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
        }
    }

    inner class EmptyVH(private val binding: ItemHomeEmptyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
            binding.tv.text = "지원한 동행요청이 없어요."
        }
    }

    companion object{
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_CONTENTS = 1
        private const val VIEW_TYPE_BOTTOM_BUTTON = 2
        private const val VIEW_TYPE_EMPTY = 3
        private val deliveryStartedStatuses = setOf(
            "DELIVERY_START",
            "PICKUP_START",
            "DELIVERY_DEPART",
            "DELIVERY_ING",
            "ING",
            "ING_START",
            "ING_DELIVERY"
        )
    }
}
