package sky.kr.co.newtogetusa.ui.main.my.profilePage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.databinding.ItemReviewBinding
import sky.kr.co.newtogetusa.databinding.ItemReviewChipBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadProfile

class ReviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<PlayerInfoDto>()
    private var reviewCodeMap: Map<String, String> = emptyMap()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ItemVH(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(itemList: List<PlayerInfoDto>, reviewCodes: List<BaseCommonDto>) {
        items.clear()
        items.addAll(itemList)
        reviewCodeMap = reviewCodes.associate { it.code to it.name }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, p1: Int) {
        (holder as ItemVH).onBindViewHolder(items[p1], p1)
    }

    inner class ItemVH(private val binding:ItemReviewBinding): BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            val review = data as? PlayerInfoDto ?: return
            val reviewer = review.user ?: review.player

            binding.flTags.removeAllViews()
            binding.ivProfile.loadProfile(reviewer?.profile_image)
            binding.tvName.text = reviewer?.nickname.orEmpty()
            binding.tvDate.text = formatDate(review.reg_date)
            binding.tvDepartAddress.text = review.depart_address.orEmpty()
            binding.tvDestAddress.text = review.dest_address.orEmpty()
            binding.tvReview.text = review.contents.orEmpty()
            bindStars(review.stars)

            val marginH = 6.dpToPx()
            val marginV = 6.dpToPx()

            review.items.mapNotNull { reviewCodeMap[it] }.forEach { reviewName ->
                val chipBinding = ItemReviewChipBinding.inflate(LayoutInflater.from(binding.root.context), binding.flTags, false)
                val lp = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(marginH, marginV, marginH, marginV)
                    flexShrink = 0f
                }
                chipBinding.root.layoutParams = lp
                (chipBinding.root as TextView).text = reviewName
                binding.flTags.addView(chipBinding.root)
            }
            binding.executePendingBindings()
        }

        private fun bindStars(stars: Int) {
            listOf(binding.ivStar1, binding.ivStar2, binding.ivStar3, binding.ivStar4, binding.ivStar5)
                .forEachIndexed { index, imageView ->
                    val drawableRes = if (index < stars) {
                        R.drawable.star_fill_priamry
                    } else {
                        R.drawable.star_fill_gray
                    }
                    imageView.setImageResource(drawableRes)
                }
        }

        private fun formatDate(rawDate: String?): String =
            rawDate.orEmpty().substringBefore(" ")
    }
}
