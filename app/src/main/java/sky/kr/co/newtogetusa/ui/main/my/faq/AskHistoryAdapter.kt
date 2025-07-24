package sky.kr.co.newtogetusa.ui.main.my.faq

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemAskHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class AskHistoryAdapter(val viewModel: AskHistoryViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = mutableListOf<Any>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemAskHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    inner class ViewHolder(val bind: ItemAskHistoryBinding):BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            bind.root.setOnClickListener {
                viewModel.onEventClick(AskHistoryViewModel.Event.AskItem(position))
            }
        }
    }

}