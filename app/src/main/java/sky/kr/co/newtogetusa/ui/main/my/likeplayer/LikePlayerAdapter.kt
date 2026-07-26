package sky.kr.co.newtogetusa.ui.main.my.likeplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.databinding.ItemLikePlayerBinding

class LikePlayerAdapter(
    private val onItemClick: (PlayerDto) -> Unit,
    private val onUnlikeClick: (PlayerDto) -> Unit
) : ListAdapter<PlayerDto, LikePlayerAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLikePlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLikePlayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlayerDto) {
            binding.item = item

            val distanceKm = item.distance / 1000.0
            binding.tvDistance.text = if (item.distance > 0) {
                String.format("%.1fkm", distanceKm)
            } else {
                ""
            }

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnUnlike.setOnClickListener { onUnlikeClick(item) }
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlayerDto>() {
        override fun areItemsTheSame(oldItem: PlayerDto, newItem: PlayerDto): Boolean {
            return oldItem.player_id == newItem.player_id
        }

        override fun areContentsTheSame(oldItem: PlayerDto, newItem: PlayerDto): Boolean {
            return oldItem == newItem
        }
    }
}
