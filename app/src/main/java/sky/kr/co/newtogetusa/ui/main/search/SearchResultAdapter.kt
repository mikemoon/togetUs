package sky.kr.co.newtogetusa.ui.main.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.databinding.ItemPlayerSearchResultBinding

class SearchResultAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<SearchResultAdapter.VH>() {

    inner class VH(val binding: ItemPlayerSearchResultBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: String){
            binding.tvName.text = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemPlayerSearchResultBinding.inflate(
        LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
}