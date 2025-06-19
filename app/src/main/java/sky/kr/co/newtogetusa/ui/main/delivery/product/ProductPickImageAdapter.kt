package sky.kr.co.newtogetusa.ui.main.delivery.product

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sky.kr.co.newtogetusa.R

class ProductPickImageAdapter(private val context: Context, private val onRemoveClick:(Uri, Int) -> Unit):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_GALLERY = 0
        const val VIEW_TYPE_IMAGE = 1
    }

    private val imageList = mutableListOf<Uri>()

    // 항목 롱클릭 리스너 인터페이스
    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    interface OnGalleryClickListener {
        fun onGalleryClick()
    }

    private var dragListener: OnStartDragListener? = null
    private var galleryClickListener: OnGalleryClickListener? = null

    fun setDragListener(listener: OnStartDragListener) {
        this.dragListener = listener
    }

    fun setGalleryClickListener(listener: OnGalleryClickListener) {
        this.galleryClickListener = listener
    }

    fun addItems(items: List<Uri>) {
        val startPosition = imageList.size + 1 // 갤러리 + 기존 아이템 개수
        imageList.addAll(items)
        notifyItemRangeInserted(startPosition, items.size)
    }

    // 데이터 설정 메서드
    fun setData(newList: List<Uri>) {
        imageList.clear()
        imageList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        // 갤러리 버튼은 제거 불가
        if (position <= 0 || position > imageList.size) {
            return
        }

        val index = position - 1
        if (index < imageList.size) {
            imageList.removeAt(index)
            notifyItemRemoved(position)
        }
    }

    // 아이템 위치 이동 메서드
    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == 0 || toPosition == 0) {
            return // 갤러리 뷰는 이동 불가
        }

        val item = imageList.removeAt(fromPosition)
        imageList.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    // 현재 리스트 반환 메서드
    fun getCurrentList(): List<Uri> = imageList.toList()

    override fun getItemViewType(position: Int): Int {
        // 첫 번째 위치는 갤러리 버튼
        return if (position == 0) VIEW_TYPE_GALLERY else VIEW_TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GALLERY ->{
                val view = LayoutInflater.from(context).inflate(R.layout.item_product_photo, parent, false)
                GalleryViewHolder(view)
            }
            else ->{
                val view = LayoutInflater.from(context).inflate(R.layout.item_product_pick_img, parent, false)
                ImageViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_GALLERY -> {
                (holder as GalleryViewHolder).bind()
            }
            VIEW_TYPE_IMAGE -> {
                // 실제 이미지 데이터 인덱스는 position - 1
                val item = imageList[position - 1]
                (holder as ImageViewHolder).bind(item, position)

                // 롱클릭 리스너 설정 (갤러리 뷰 제외)
                holder.itemView.setOnLongClickListener {
                    dragListener?.onStartDrag(holder)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = imageList.size + 1 // 갤러리 버튼 포함

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            // 클릭 리스너 설정
            itemView.setOnClickListener {
                galleryClickListener?.onGalleryClick()
            }
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivImg)
        private val closeView: ImageView = itemView.findViewById(R.id.ivClose)

        fun bind(item: Uri, position:Int) {

            // Glide로 이미지 로딩
            Glide.with(itemView.context)
                .load(item)
                .centerCrop()
                .into(imageView)
            closeView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onRemoveClick(item, pos)
                }
            }
        }
    }
}