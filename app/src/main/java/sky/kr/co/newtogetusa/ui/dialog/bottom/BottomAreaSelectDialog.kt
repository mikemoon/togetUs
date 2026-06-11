package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.graphics.Rect
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.databinding.DialogBottomAreaBinding
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class BottomAreaSelectDialog(private val selectedRegionCallback:(List<RegionDto>, List<RegionDto>) -> Unit) : BottomBaseDialog<DialogBottomAreaBinding, BottomAreaSelectViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_area
    override val viewModel: BottomAreaSelectViewModel by viewModels()

    private lateinit var areaAdapter: BottomAreaAdapter
    var selectedRegion: List<RegionDto>? = null
    var selectedSubRegion: List<RegionDto>? = null

    override fun init() {
        super.init()

        viewModel.getDomesticAddressList()
        viewModel.getDomesticSubAddressList()

        areaAdapter =
            BottomAreaAdapter(regionItems(), multiSelect = false) { selectRegion, isDetail ->
                if(selectRegion.isNotEmpty()) {
                    if (isDetail) {
                        dataBinding.tvRegion2.text = selectRegion[0].name
                        selectedSubRegion = selectRegion
                    } else {
                        selectedRegion = selectRegion
                        if (selectRegion[0].isAllRegion()) {
                            dataBinding.tvDetailDesc.isVisible = false
                            dataBinding.tvRegion1.text = if (viewModel.isLocal.value) "국내" else "해외"
                            dataBinding.tvRegion2.text = ALL_REGION.name
                            selectedSubRegion = listOf(ALL_REGION)
                        } else {
                            dataBinding.tvDetailDesc.apply {
                                isVisible = true
                                text =
                                    if (viewModel.isLocal.value) "시/군/구는 복수로 선택할 수 있어요" else "도시는 복수로 선택할 수 있어요"
                            }
                            dataBinding.tvRegion1.text = selectRegion[0].name
                            dataBinding.tvRegion2.text = if (viewModel.isLocal.value) "시/군/구" else "도시"
                            selectedSubRegion = null
                            (dataBinding.rvRegions.adapter as BottomAreaAdapter).update(
                                subRegionItems(selectRegion[0]),
                                multiSelect = true,
                                isDetail = true
                            )
                        }
                    }
                }
            }
        dataBinding.rvRegions.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = areaAdapter
            addItemDecoration(GridSpacingItemDecoration(3, 12.dpToPx(), true))
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLocal.collectLatest { isLocal ->
                    dataBinding.tvLocal.isSelected = isLocal
                    dataBinding.tvForeign.isSelected = !isLocal
                    areaAdapter.update(
                        regionItems(),
                        multiSelect = false,
                        isDetail = false
                    )
                    dataBinding.tvRegion1.text = if (isLocal) "시/도" else "국가"
                    dataBinding.tvRegion2.text = if (isLocal) "시/군/구" else "도시"
                    dataBinding.tvDetailDesc.isVisible = false
                    selectedRegion = null
                    selectedSubRegion = null
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedCompleteFlow.collectLatest {
                        if(it){
                            selectedRegionCallback(selectedRegion ?: listOf(ALL_REGION), selectedSubRegion ?: emptyList())
                            dismissAllowingStateLoss()
                        }
                    }
                }
                launch {
                    viewModel.domesticAddressList.collectLatest {
                        areaAdapter.setItems(regionItems())
                    }
                }
            }
        }
    }

    private fun regionItems(): List<RegionDto> =
        withAllRegion(viewModel.domesticAddressList.value)

    private fun subRegionItems(region: RegionDto): List<RegionDto> =
        withAllRegion(if (viewModel.isLocal.value) {
            viewModel.domesticSubAddressList.value.filter { it.cate == region.code }
        } else {
            viewModel.domesticSubAddressList.value
        })

    private fun withAllRegion(items: List<RegionDto>): List<RegionDto> =
        listOf(ALL_REGION) + items.filterNot { it.isAllRegion() }

    private fun RegionDto.isAllRegion(): Boolean =
        name == ALL_REGION.name || code.isBlank()

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }

    companion object {
        private val ALL_REGION = RegionDto(cate = "", code = "", name = "전체", description = null)
    }
}
