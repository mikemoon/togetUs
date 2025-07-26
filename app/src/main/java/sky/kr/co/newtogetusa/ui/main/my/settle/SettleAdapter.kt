package sky.kr.co.newtogetusa.ui.main.my.settle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemSettleContentBinding
import sky.kr.co.newtogetusa.databinding.ItemSettleDateBinding
import sky.kr.co.newtogetusa.databinding.ItemSettleTopBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class SettleAdapter(val viewModel: SettleViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TOP_INFO -> TopViewHolder(ItemSettleTopBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            DATE_INFO -> DateInfoViewHolder(ItemSettleDateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ContentViewHolder(ItemSettleContentBinding.inflate(LayoutInflater.from(parent.context),parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> return TOP_INFO
            1 -> return DATE_INFO
            else -> return CONTENT
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    fun setItems(items: List<Any>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(position){
            0 -> (holder as TopViewHolder).onBindViewHolder(items[position], position)
            1 -> (holder as DateInfoViewHolder).onBindViewHolder(items[position], position)
            else -> (holder as ContentViewHolder).onBindViewHolder(items[position], position)
        }
    }

    inner class TopViewHolder(val bind: ItemSettleTopBinding) : BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
            bind.viewModel = viewModel
        }
    }

    inner class DateInfoViewHolder(val bind: ItemSettleDateBinding) : BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)

            bind.root.setOnClickListener {
                viewModel.onEventClick(SettleViewModel.Event.Date)
            }
        }
    }

    inner class ContentViewHolder(val bind: ItemSettleContentBinding) : BaseViewHolder(bind.root){
        override fun onBindViewHolder(data: Any?, position: Int) {
            super.onBindViewHolder(data, position)
        }
    }

    companion object{
        const val TOP_INFO = 0
        const val DATE_INFO = 1
        const val CONTENT = 2
    }
}