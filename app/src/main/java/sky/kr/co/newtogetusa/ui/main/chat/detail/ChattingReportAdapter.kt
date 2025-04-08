package sky.kr.co.newtogetusa.ui.main.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemChatReportReasonBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class ChattingReportAdapter(val viewModel: ChattingReportViewModel) : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ReportReasonModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemChatReportReasonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) = holder.onBindViewHolder(items[position], position)

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding:ItemChatReportReasonBinding):BaseViewHolder(binding.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            if(data !is ReportReasonModel) return
            binding.viewModel = viewModel
            binding.item = data
            binding.root.setOnClickListener {
                viewModel.onEventClick(ChattingReportViewModel.Event.Reason(data.id, data.reason))
            }
        }
    }
}