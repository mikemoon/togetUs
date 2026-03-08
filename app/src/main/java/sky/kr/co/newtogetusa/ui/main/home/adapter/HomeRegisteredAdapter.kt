package sky.kr.co.newtogetusa.ui.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.databinding.ItemHomeBottomButtonBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeContentsBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeEmptyBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeTitleBinding
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter.BottomButtonVH
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter.Companion
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter.ContentsVH
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter.EmptyVH
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter.TitleVH
import sky.kr.co.newtogetusa.utils.loadImage

class HomeRegisteredAdapter(private val onItemClickListener: ((DeliverySummaryDto) -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<DeliverySummaryDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_TITLE -> TitleVH(ItemHomeTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_EMPTY -> EmptyVH(ItemHomeEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_BOTTOM_BUTTON -> BottomButtonVH(ItemHomeBottomButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ContentsVH(ItemHomeContentsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return when {
            items.isEmpty() -> 2 // title + empty
            items.size >= 3 -> items.size + 2 // title + contents + bottombutton
            else -> items.size + 1 // title + contents만
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VIEW_TYPE_TITLE
            items.isEmpty() -> VIEW_TYPE_EMPTY
            items.size >= 3 && position == items.size + 1 -> VIEW_TYPE_BOTTOM_BUTTON
            else -> VIEW_TYPE_CONTENTS
        }
    }

    fun setItems(items: List<DeliverySummaryDto>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
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
            binding.tvTitle.text = "등록한 진행요청"
        }
    }

    inner class ContentsVH(private val binding: ItemHomeContentsBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: DeliverySummaryDto) {
            binding.ivProduct.loadImage(item.prd_picture)
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }

    inner class BottomButtonVH(private val binding: ItemHomeBottomButtonBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
        }
    }

    inner class EmptyVH(private val binding: ItemHomeEmptyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
            binding.tv.text = "등록한 진행요청이 없어요."
        }
    }

    companion object{
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_CONTENTS = 1
        private const val VIEW_TYPE_BOTTOM_BUTTON = 2
        private const val VIEW_TYPE_EMPTY = 3
    }
}