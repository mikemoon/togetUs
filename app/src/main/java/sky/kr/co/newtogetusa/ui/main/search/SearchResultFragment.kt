package sky.kr.co.newtogetusa.ui.main.search

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentSearchResultBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomFilterDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class SearchResultFragment  : BaseFragment<FragmentSearchResultBinding, SearchResultViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_search_result
    override val viewModel: SearchResultViewModel by viewModels()

    var departList : List<String>? = null
    var destList : List<String>? = null
    private val args: SearchResultFragmentArgs by navArgs()
    private val searchResultAdapter = SearchResultAdapter()
    private var searchJob: Job? = null

    override fun init() {
        super.init()
        dataBinding.rv.adapter = searchResultAdapter

        searchResultAdapter.addLoadStateListener { loadState ->
            val isEmpty = loadState.refresh is LoadState.NotLoading && searchResultAdapter.itemCount == 0
            dataBinding.llEmpty.isVisible = isEmpty
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchPlayers(
                    departCd = args.departCd.toList(),
                    destCd = args.destCd.toList(),
                    sortType = "distance"
                ).collect(searchResultAdapter::submitData)
            }
        }

        searchPlayersWithSortType(SORT_DISTANCE)
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                SearchResultViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                SearchResultViewModel.Event.Filter ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomFilterDialog().apply {
                            itemSelectCallback = { selectedItem ->
                                this@SearchResultFragment.dataBinding.tvFilter.text = selectedItem
                                searchPlayersWithSortType(selectedItem.toSortType())
                            }
                        }
                    )
                }
            }
        }
    }

    private fun searchPlayersWithSortType(sortType: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchPlayers(
                    departCd = args.departCd.toList(),
                    destCd = args.destCd.toList(),
                    sortType = sortType
                ).collect(searchResultAdapter::submitData)
            }
        }
    }

    private fun String.toSortType(): String {
        return when (this) {
            FILTER_DISTANCE -> SORT_DISTANCE
            FILTER_STAR_RATING -> SORT_STAR_RATING
            FILTER_DEAL_COUNT -> SORT_DEAL_COUNT
            FILTER_LATEST_DEAL -> SORT_LATEST_DEAL
            else -> SORT_DISTANCE
        }
    }

    companion object {
        private const val FILTER_DISTANCE = "거리순"
        private const val FILTER_STAR_RATING = "별점순"
        private const val FILTER_DEAL_COUNT = "거래건순"
        private const val FILTER_LATEST_DEAL = "최근 거래순"

        private const val SORT_DISTANCE = "DISTANCE"
        private const val SORT_STAR_RATING = "STAR_RATING"
        private const val SORT_DEAL_COUNT = "DEAL_COUNT"
        private const val SORT_LATEST_DEAL = "LATEST_DEAL"
    }
}