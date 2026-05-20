package sky.kr.co.newtogetusa.ui.main.history.status

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryStatusLogDto
import sky.kr.co.newtogetusa.databinding.ItemDeliveryStatusLogBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage

class DeliveryStatusAdapter : RecyclerView.Adapter<DeliveryStatusAdapter.VH>() {

    private var items: List<DeliveryStatusLogDto> = emptyList()

    fun submitItems(newItems: List<DeliveryStatusLogDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemDeliveryStatusLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position == 0)
    }

    override fun getItemCount(): Int = items.size

    inner class VH(private val binding: ItemDeliveryStatusLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeliveryStatusLogDto, isFirst: Boolean) = with(binding) {
            tvStatus.text = item.statusName.orEmpty().ifBlank { item.statusCd.orEmpty() }
            tvDate.text = item.regDate.orEmpty()
            tvActor.text = item.actor.orEmpty()
            vTopLine.isVisible = isFirst

            val pictures = item.pictures.orEmpty()
            val imageViews = listOf(ivPhoto1, ivPhoto2, ivPhoto3)
            imageViews.forEachIndexed { index, imageView ->
                val url = pictures.getOrNull(index)
                imageView.isVisible = url != null
                imageView.loadImage(url, roundedCorner = 8.dpToPx(), error = R.drawable.no_img)
            }
            tvMoreCount.isVisible = pictures.size > imageViews.size
            tvMoreCount.text = "+${pictures.size - imageViews.size}"
            llPictures.isVisible = pictures.isNotEmpty()
        }
    }
}
