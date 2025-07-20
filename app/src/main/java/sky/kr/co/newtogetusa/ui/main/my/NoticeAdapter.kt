package sky.kr.co.newtogetusa.ui.main.my

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemNoticeBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class NoticeAdapter(private val viewModel: NoticeViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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


    inner class ViewHolder(private val bind : ItemNoticeBinding):BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            bind.tvTitle.text = "배송 수수료 안내"
            bind.tvDate.text = "2025.08.20"
            bind.root.setOnClickListener {
                viewModel.onEventClick(NoticeViewModel.Event.NoticeDetail(0))
            }
        }
    }
}