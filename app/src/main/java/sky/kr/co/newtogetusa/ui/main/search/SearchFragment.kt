package sky.kr.co.newtogetusa.ui.main.search

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.databinding.FragmentSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomAreaSelectDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding, SearchViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_search
    override val viewModel: SearchViewModel by viewModels()

    private var departCd: List<String> = emptyList()
    private var destCd: List<String> = emptyList()
    private var isDomestic: Boolean = true

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer.collectLatest { isModePlayer ->
                    Timber.d("isModePlayer $isModePlayer")
                    if (isModePlayer == true && findNavController().currentDestination?.id == R.id.searchFragment) {
                        findNavController().navigate(R.id.action_searchFragment_to_deliveryRequestSearchFragment)
                    }
                }
            }
        }

        viewModel.search.observe(viewLifecycleOwner){

        }

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                SearchViewModel.Event.StartRegion -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog{ selectedRegion, selectedSubRegion ->
                            updateAreaSelection(
                                isDepart = true,
                                selectedRegion = selectedRegion,
                                selectedSubRegion = selectedSubRegion
                            )
                        }
                    )
                }
                SearchViewModel.Event.DestinationRegion -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog{ selectedRegion, selectedSubRegion ->
                            updateAreaSelection(
                                isDepart = false,
                                selectedRegion = selectedRegion,
                                selectedSubRegion = selectedSubRegion
                            )
                        }
                    )
                }
                SearchViewModel.Event.Search -> {
                    findNavController().navigate(
                        SearchFragmentDirections.actionSearchFragmentToSearchResultFragment(
                            departCd = departCd.toTypedArray(),
                            destCd = destCd.toTypedArray(),
                            isDomestic = isDomestic
                        )
                    )
                    //findNavController().navigate(R.id.playerTermFragment)
                }
            }
        }
    }

    fun List<String>.toRegionSummary(): String {
        val sorted = this.sorted()
        return if (sorted.size > 1) {
            "${sorted.first()} 외 +${sorted.size - 1}"
        } else {
            sorted.firstOrNull() ?: ""
        }
    }

    private fun updateAreaSelection(
        isDepart: Boolean,
        selectedRegion: List<RegionDto>,
        selectedSubRegion: List<RegionDto>
    ) {
        val isAllSelected = selectedRegion.any { it.isAllRegion() } || selectedSubRegion.any { it.isAllRegion() }
        val regionName = selectedRegion.firstOrNull { !it.isAllRegion() }?.name ?: "국내"
        val selectedCodes = if (isAllSelected) {
            emptyList()
        } else {
            selectedSubRegion.map { it.code }.filter { it.isNotBlank() }
        }
        val subRegionText = if (isAllSelected) {
            "전체"
        } else {
            selectedSubRegion.map { it.name }.toRegionSummary()
        }
        val hasSelection = isAllSelected || selectedCodes.isNotEmpty()

        if (isDepart) {
            dataBinding.tvStart.text = "$regionName > $subRegionText"
            departCd = selectedCodes
            viewModel.setDepartAreaSelected(hasSelection)
        } else {
            dataBinding.tvDestination.text = "$regionName > $subRegionText"
            destCd = selectedCodes
            viewModel.setDestinationAreaSelected(hasSelection)
        }
    }

    private fun RegionDto.isAllRegion(): Boolean =
        name == "전체" || code.isBlank()
}
