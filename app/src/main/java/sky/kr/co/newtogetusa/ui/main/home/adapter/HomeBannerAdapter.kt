package sky.kr.co.newtogetusa.ui.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.users.BannerDto
import sky.kr.co.newtogetusa.databinding.ItemHomeBannerBinding
import sky.kr.co.newtogetusa.utils.loadImage

class HomeBannerAdapter(
    private val onClick: (BannerDto) -> Unit
) : RecyclerView.Adapter<HomeBannerAdapter.BannerViewHolder>() {

    private val items = mutableListOf<BannerDto>()

    fun setItems(banners: List<BannerDto>) {
        items.clear()
        items.addAll(banners)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemHomeBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val item = items[position]
        holder.binding.ivBanner.loadImage(
            item.imageUrl,
            placeholder = R.drawable.temp_banner_area,
            error = R.drawable.temp_banner_area
        )
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class BannerViewHolder(val binding: ItemHomeBannerBinding) : RecyclerView.ViewHolder(binding.root)
}