package sky.kr.co.newtogetusa.ui.main.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemHistoryDetailPhotosBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage

class HistoryDetailPhotoAdapter(private val itemSelect:(String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return VH(ItemHistoryDetailPhotosBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) = (holder as VH).bind(items[position], position)

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(uriItems:List<String>){
        items.clear()
        items.addAll(uriItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemHistoryDetailPhotosBinding): RecyclerView.ViewHolder(b.root){
        fun bind(url:String, position: Int){
            b.iv.loadImage(url, roundedCorner = 4.dpToPx())
            b.root.setOnClickListener {
                itemSelect.invoke(url)
            }
        }
    }

}