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
                            val subRegionText = selectedSubRegion.map { it.name }.toRegionSummary()
                            dataBinding.tvStart.text = "${selectedRegion[0].name} > $subRegionText"
                            departCd = selectedSubRegion.map { it.code }
                        }
                    )
                }
                SearchViewModel.Event.DestinationRegion -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog{ selectedRegion, selectedSubRegion ->
                            val subRegionText = selectedSubRegion.map { it.name }.toRegionSummary()
                            dataBinding.tvDestination.text = "${selectedRegion[0].name} > ${subRegionText}"
                            destCd = selectedSubRegion.map { it.code }
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
}