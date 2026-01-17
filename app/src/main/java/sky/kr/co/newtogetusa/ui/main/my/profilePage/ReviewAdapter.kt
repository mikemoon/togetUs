package sky.kr.co.newtogetusa.ui.main.my.profilePage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import sky.kr.co.newtogetusa.databinding.ItemReviewBinding
import sky.kr.co.newtogetusa.databinding.ItemReviewChipBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.utils.dpToPx

class ReviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ItemVH(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(itemList :List<String>){
        items.clear()
        items.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, p1: Int) {
        (holder as ItemVH).onBindViewHolder(items[p1], p1)
    }

    inner class ItemVH(private val binding:ItemReviewBinding): BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)

            binding.flTags.removeAllViews()

            val marginH = 6.dpToPx()
            val marginV = 6.dpToPx()

            val reviews = listOf("시간 약속을 잘 지켰어요.", "빠르게 응답했어요.", "매너가 좋았어요", "매너가 좋았어요2")

            reviews.forEach { review ->
                val chipBinding = ItemReviewChipBinding.inflate(LayoutInflater.from(binding.root.context), binding.flTags, false)
                val lp = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(marginH, marginV, marginH, marginV)
                    flexShrink = 0f
                }
                chipBinding.root.layoutParams = lp
                (chipBinding.root as TextView).text = review
                binding.flTags.addView(chipBinding.root)
            }
            binding.executePendingBindings()
        }
    }
}