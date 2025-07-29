package sky.kr.co.newtogetusa.ui.main.home.playerAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemHomeBottomButtonBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeContentsBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeEmptyBinding
import sky.kr.co.newtogetusa.databinding.ItemHomeTitleBinding

class AvailableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()

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
        if(items.size == 0) return 2 else return items.size + 2
    }

    fun setItems(items: List<Any>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) VIEW_TYPE_TITLE else{
            if(items.size == 0){
                VIEW_TYPE_EMPTY
            }else{
                if(position == items.size + 1) VIEW_TYPE_BOTTOM_BUTTON else
                    VIEW_TYPE_CONTENTS
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            VIEW_TYPE_TITLE -> (holder as TitleVH).bind("")
            VIEW_TYPE_EMPTY -> (holder as EmptyVH).bind("")
            VIEW_TYPE_BOTTOM_BUTTON -> (holder as BottomButtonVH).bind("")
            else -> (holder as ContentsVH).bind(items[position-1] as String)
        }
    }

    inner class TitleVH(private val binding: ItemHomeTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.tvTitle.text = "배송 가능한 요청"
            binding.llMore.isVisible = true
        }
    }

    inner class ContentsVH(private val binding: ItemHomeContentsBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {

        }
    }

    inner class BottomButtonVH(private val binding: ItemHomeBottomButtonBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
        }
    }

    inner class EmptyVH(private val binding: ItemHomeEmptyBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
            binding.tv.text = "가능한 배송이  없어요."
        }
    }

    companion object{
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_CONTENTS = 1
        private const val VIEW_TYPE_BOTTOM_BUTTON = 2
        private const val VIEW_TYPE_EMPTY = 3
    }
}