package sky.kr.co.newtogetusa.ui.main.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.databinding.ItemPlayerSearchResultBinding
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.loadProfile

class SearchResultAdapter(
    private val onItemClick: (PlayerDto) -> Unit
) : PagingDataAdapter<PlayerDto, SearchResultAdapter.VH>(diff)  {

    inner class VH(private val binding: ItemPlayerSearchResultBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(item: PlayerDto?) {
            binding.tvName.text = item?.nickname.orEmpty()
            binding.ivProfile.loadProfile(item?.profile_image)
            binding.tvScore.text = item?.star_average.toString()
            binding.tvDoneCnt.text = "(${item?.complete_count.toString()})"
            binding.root.setOnClickListener {
                item?.let(onItemClick)
            }
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