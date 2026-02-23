package sky.kr.co.newtogetusa.ui.main.history

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.utils.loadImage

class PhotoPagerAdapter(private val items: List<String>
) : RecyclerView.Adapter<PhotoPagerAdapter.VH>() {

    inner class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val iv = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return VH(iv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.iv.loadImage(items[position])
    }

    override fun getItemCount() = items.size
}