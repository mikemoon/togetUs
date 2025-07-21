package sky.kr.co.newtogetusa.ui.main.my.faq

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemFaqListBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class FAQListAdapter(val viewModel: FAQViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemFaqListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<String>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = (holder as ViewHolder).onBindViewHolder(items[position], position)

    inner class  ViewHolder(private val binding: ItemFaqListBinding) : BaseViewHolder(binding.root) {
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            binding.data = data as String
            binding.root.setOnClickListener {
                viewModel.onEventClick(FAQViewModel.Event.FAQDetail(position))
            }
        }
    }
}