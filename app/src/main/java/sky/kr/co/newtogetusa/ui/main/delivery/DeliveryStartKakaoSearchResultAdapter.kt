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
            binding.tvSearchAddress.text = doc.subtitle ?: doc.name
            binding.tvRoadAddress.text = if(doc.roadAddress == "") doc.name else doc.roadAddress
            binding.tvJibun.text = doc.name
            binding.tvDistance.text = doc.distance?.let { "${it}m" }.orEmpty()
            val hasDistance = !doc.distance.isNullOrBlank()
            binding.vDot.isVisible = hasDistance
            binding.tvDistance.isVisible = hasDistance


            // 좌표가 필요하면 여기서 변환
            val lat = doc.lat
            val lng = doc.lng

            binding.root.setOnClickListener {
                // Kakao 응답에는 placeId가 없음
                onClick(doc)
            }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<KakaoSearchModel>() {
            override fun areItemsTheSame(old: KakaoSearchModel, new: KakaoSearchModel): Boolean {
                // 주소 문자열 + 좌표로 동일성 판단(필요 시 더 엄격하게)
                return old.name == new.name && old.lat == new.lat && old.lng == new.lng
            }
            override fun areContentsTheSame(old: KakaoSearchModel, new: KakaoSearchModel) = old == new
        }
    }
}