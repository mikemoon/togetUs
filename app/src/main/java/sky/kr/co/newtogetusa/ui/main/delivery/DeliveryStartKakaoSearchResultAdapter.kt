package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoSearchAddressResponse
import sky.kr.co.newtogetusa.databinding.ItemSearchResultBinding
import sky.kr.co.newtogetusa.ui.base.BaseViewHolder

class DeliveryStartKakaoSearchResultAdapter(
    private val onClick: (KakaoSearchModel) -> Unit
) : PagingDataAdapter<KakaoSearchModel, DeliveryStartKakaoSearchResultAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val doc = getItem(position) ?: return
        holder.bind(doc)
    }

    inner class VH(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doc: KakaoSearchModel) {
            // iOS 스타일: placeName이 있으면 placeName, 없으면 addressName을 제목으로
            // subtitle이 있으면 subtitle (place name), 없으면 name (address name)
            binding.tvSearchAddress.text = doc.subtitle ?: doc.name

            // iOS 스타일: 주소는 항상 addressName 표시
            // roadAddress가 있으면 roadAddress, 없으면 name
            binding.tvRoadAddress.text = if (doc.roadAddress.isNullOrEmpty()) doc.name else doc.roadAddress

            // iOS에서는 사용하지 않는 필드들 - 숨김
            binding.tvJibun.isVisible = false
            binding.vDot.isVisible = false
            binding.tvDistance.isVisible = false

            binding.root.setOnClickListener {
                onClick(doc)
            }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<KakaoSearchModel>() {
            override fun areItemsTheSame(old: KakaoSearchModel, new: KakaoSearchModel): Boolean {
                return old.name == new.name && old.lat == new.lat && old.lng == new.lng
            }
            override fun areContentsTheSame(old: KakaoSearchModel, new: KakaoSearchModel) = old == new
        }
    }
}