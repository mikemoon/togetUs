package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.databinding.ItemRegionChipBinding
import timber.log.Timber

class BottomAreaAdapter(
    private var regions: List<RegionDto>,
    private var multiSelect: Boolean = false,        // 단일 / 복수 선택 모드 분기
    private val onClick: (selected: List<RegionDto>, isDetail: Boolean) -> Unit
) : RecyclerView.Adapter<BottomAreaAdapter.VH>() {

    //지역 상세 단계 인지
    private var isDetail = false

    // 단일 선택인 경우
    private var selectedPosition = 0

    // 복수 선택인 경우
    private val selectedPositions = mutableSetOf<Int>()

    inner class VH(val binding: ItemRegionChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RegionDto, pos: Int) {
            binding.chipRegion.text = data.name

            // isSelected 표시
            binding.chipRegion.isSelected = if (multiSelect) {
                selectedPositions.contains(pos)
            } else {
                pos == selectedPosition
            }

            binding.chipRegion.setOnClickListener {
                if (multiSelect) {
                    val allPosition = regions.indexOfFirst { it.isAllRegion() }
                    if (data.isAllRegion()) {
                        selectedPositions.clear()
                        selectedPositions.add(pos)
                        notifyDataSetChanged()
                    } else {
                        if (allPosition >= 0) selectedPositions.remove(allPosition)
                        if (!selectedPositions.remove(pos)) {
                            selectedPositions.add(pos)
                        }
                        // 클릭된 아이템만 갱신
                        notifyItemChanged(pos)
                        if (allPosition >= 0) notifyItemChanged(allPosition)
                    }

                } else {
                    // 기존 단일 선택 로직
                    val old = selectedPosition
                    selectedPosition = pos
                    notifyItemChanged(old)
                    notifyItemChanged(pos)
                }

                // 선택된 이름 리스트를 콜백
                val currentSelection = if (multiSelect) {
                    selectedPositions.map { regions[it] }
                } else {
                    listOf(regions[selectedPosition])
                }
                onClick(currentSelection, isDetail)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemRegionChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = regions.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(regions: List<RegionDto>){
        this.regions = regions
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(regions[position], position)

    @SuppressLint("NotifyDataSetChanged")
    fun update(regions: List<RegionDto>, multiSelect: Boolean, isDetail: Boolean = false) {
        Timber.d("update regions: $regions")
        this.regions = regions
        this.multiSelect = multiSelect
        this.isDetail = isDetail
        // 선택 상태 초기화
        selectedPosition = 0
        selectedPositions.clear()
        notifyDataSetChanged()
    }

    private fun RegionDto.isAllRegion(): Boolean =
        name == "전체" || code.isBlank()
}
