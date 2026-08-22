package sky.kr.co.newtogetusa.ui.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySearchResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemHomeContentsBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeEmptyBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeTitleBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage

class HomeProgressAdapter(private val onSelect:(DeliverySummaryDto) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<DeliverySummaryDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_TITLE -> TitleVH(ItemHomeTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_EMPTY -> EmptyVH(ItemHomeEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ContentsVH(ItemHomeContentsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun setItems(list: List<DeliverySummaryDto>, hasMore: Boolean = false){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        // iOS와 동일: 하단 더보기 버튼은 표시하지 않는다
        return when {
            items.isEmpty() -> 2
            else -> items.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) VIEW_TYPE_TITLE else{
            if(items.size == 0){
                VIEW_TYPE_EMPTY
            }else{
                VIEW_TYPE_CONTENTS
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            VIEW_TYPE_TITLE -> (holder as TitleVH).bind("")
            VIEW_TYPE_EMPTY -> (holder as EmptyVH).bind("")
            else -> (holder as ContentsVH).bind(items[position-1])
        }
    }

    inner class TitleVH(private val binding: ItemHomeTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.tvTitle.text = "진행 중인 동행요청"
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
                onSelect.invoke(item)
            }
        }

        private fun bindAddress(item: DeliverySummaryDto) {
            val showDestination = item.status_cd in deliveryStartedStatuses
            binding.tvTarget.text = if (showDestination) "도착지" else "픽업지"
            binding.tvAddress.text = if (showDestination) item.dest_address else item.depart_address
        }
    }

    inner class EmptyVH(private val binding: ItemHomeEmptyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
            binding.tv.text = "진행중인 동행요청이 없어요."
        }
    }

    companion object{
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_CONTENTS = 1
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
