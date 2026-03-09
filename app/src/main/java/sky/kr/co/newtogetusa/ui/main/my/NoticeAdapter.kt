package sky.kr.co.newtogetusa.ui.main.my

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.my.NoticeDto
import sky.kr.co.newtogetusa.databinding.ItemNoticeBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class NoticeAdapter(private val viewModel: NoticeViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<NoticeDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<NoticeDto>){
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
            val notice = data as NoticeDto
            bind.tvTitle.text = notice.title
            bind.tvDate.text = notice.regDate
            bind.root.setOnClickListener {
                viewModel.onEventClick(NoticeViewModel.Event.NoticeDetail(notice.noticeId))
            }
        }
    }
}