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
            BottomAreaAdapter(viewModel.domesticAddressList.value, multiSelect = false) { selectRegion, isDetail ->
                if(selectRegion.isNotEmpty()) {
                    if (isDetail) {
                        dataBinding.tvRegion2.text = selectRegion[0].name
                        selectedSubRegion = selectRegion
                    } else {
                        dataBinding.tvDetailDesc.apply {
                            isVisible = true
                            text =
                                if (viewModel.isLocal.value) "시/군/구는 복수로 선택할 수 있어요" else "도시는 복수로 선택할 수 있어요"
                        }
                        dataBinding.tvRegion1.text = selectRegion[0].name
                        (dataBinding.rvRegions.adapter as BottomAreaAdapter).update(
                            if (viewModel.isLocal.value) viewModel.domesticSubAddressList.value.filter { it.cate == selectRegion[0].code } else viewModel.domesticSubAddressList.value,
                            multiSelect = true,
                            isDetail = true
                        )
                        selectedRegion = selectRegion
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
                        if (isLocal) viewModel.domesticSubAddressList.value else viewModel.domesticSubAddressList.value,
                        multiSelect = false,
                        isDetail = false
                    )
                    dataBinding.tvRegion1.text = if (isLocal) "시/도" else "국가"
                    dataBinding.tvRegion2.text = if (isLocal) "시/군/구" else "도시"
                    dataBinding.tvDetailDesc.isVisible = false
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedCompleteFlow.collectLatest {
                        if(it){
                            selectedRegionCallback(selectedRegion ?: emptyList(), selectedSubRegion ?: emptyList())
                            dismissAllowingStateLoss()
                        }
                    }
                }
                launch {
                    viewModel.domesticAddressList.collectLatest {
                        areaAdapter.setItems(it)
                    }
                }
            }
        }
    }

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
}