package sky.kr.co.newtogetusa.ui.main.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.databinding.ItemPlayerSearchResultBinding

class SearchResultAdapter(
) : PagingDataAdapter<PlayerDto, SearchResultAdapter.VH>(diff)  {

    inner class VH(private val binding: ItemPlayerSearchResultBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: PlayerDto?) {
            binding.tvName.text = item?.nickname.orEmpty()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemPlayerSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val diff = object : DiffUtil.ItemCallback<PlayerDto>() {
            override fun areItemsTheSame(oldItem: PlayerDto, newItem: PlayerDto): Boolean =
                oldItem.player_id == newItem.player_id

            override fun areContentsTheSame(oldItem: PlayerDto, newItem: PlayerDto): Boolean =
                oldItem == newItem
        }
    }
}