package sky.kr.co.newtogetusa.ui.main.my.faq

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQCateDto
import sky.kr.co.newtogetusa.databinding.ItemFaqBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class FAQAdapter(
    private val onCategoryClick: (FAQCateDto) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<FAQCateDto>()
    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<FAQCateDto>){
        this.items.clear()
        this.items.addAll(items)
        selectedPosition = 0
        notifyDataSetChanged()
    }

    fun selectCategory(cateId: Int) {
        val newPosition = items.indexOfFirst { it.cateId == cateId }
        if (newPosition == -1 || newPosition == selectedPosition) return

        val oldPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindViewHolder(items[position], position)
    }

    inner class ViewHolder(private val bind: ItemFaqBinding): BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            val category = data as FAQCateDto
            bind.data = category.cateName
            bind.root.isSelected = position == selectedPosition
            bind.root.setOnClickListener {
                onCategoryClick(category)
                selectCategory(category.cateId)
            }
        }
    }
}