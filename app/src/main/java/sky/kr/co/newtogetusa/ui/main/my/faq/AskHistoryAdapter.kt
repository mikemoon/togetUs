package sky.kr.co.newtogetusa.ui.main.my.faq

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.my.InquiryDto
import sky.kr.co.newtogetusa.databinding.ItemAskHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class AskHistoryAdapter(val viewModel: AskHistoryViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = mutableListOf<InquiryDto>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ItemAskHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<InquiryDto>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindViewHolder(items[position], position)
    }

    inner class ViewHolder(val bind: ItemAskHistoryBinding) : BaseViewHolder(bind.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            val item = data as InquiryDto
            bind.tvState.text = when (item.status) {
                "WAIT" -> "답변대기"
                else -> "답변완료"
            }
            bind.tvState.setTextColor(bind.root.context.getColor(if (item.status == "WAIT")R.color.primary_100 else R.color.black_60))
            bind.tvState.background =
                bind.root.context.getDrawable(if (item.status == "WAIT") R.drawable.background_s_primary5_r4 else R.drawable.background_s_b5_r4)
            bind.tvTitle.text = item.title
            bind.tvDate.text = item.inquiryDate
            bind.root.setOnClickListener {
                viewModel.onEventClick(AskHistoryViewModel.Event.AskItem(item))
            }
        }
    }

}