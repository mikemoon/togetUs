package sky.kr.co.newtogetusa.ui.main.my.faq

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemFaqBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class FAQAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<Any>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindViewHolder(items[position], position)
    }

    inner class ViewHolder(private val bind: ItemFaqBinding): BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            bind.data = data as String
            bind.root.isSelected = position == 0
        }
    }
}