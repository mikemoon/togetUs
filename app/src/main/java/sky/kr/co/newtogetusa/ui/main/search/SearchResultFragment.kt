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
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentSearchResultBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomFilterDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class SearchResultFragment  : BaseFragment<FragmentSearchResultBinding, SearchResultViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_search_result
    override val viewModel: SearchResultViewModel by viewModels()

    private val args: SearchResultFragmentArgs by navArgs()
    private val searchResultAdapter = SearchResultAdapter(::navigateToProfileManagement)

    override fun init() {
        super.init()
        dataBinding.rv.adapter = searchResultAdapter

        searchResultAdapter.addLoadStateListener { loadState ->
            val isEmpty = loadState.refresh is LoadState.NotLoading && searchResultAdapter.itemCount == 0
            dataBinding.llEmpty.isVisible = isEmpty
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playerPagingData.collect(searchResultAdapter::submitData)
            }
        }

        viewModel.updateSearchCondition(
            departCd = args.departCd.toList(),
            destCd = args.destCd.toList(),
            isDomestic = args.isDomestic,
            sortType = SORT_STAR_RATING
        )
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
                            filterList = PLAYER_SEARCH_FILTERS
                            itemSelectCallback = { selectedItem ->
                                this@SearchResultFragment.dataBinding.tvFilter.text = selectedItem
                                this@SearchResultFragment.viewModel.updateSearchCondition(
                                    departCd = args.departCd.toList(),
                                    destCd = args.destCd.toList(),
                                    isDomestic = args.isDomestic,
                                    sortType = selectedItem.toSortType()
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun String.toSortType(): String {
        return when (this) {
            FILTER_DISTANCE -> SORT_DISTANCE
            FILTER_STAR_RATING -> SORT_STAR_RATING
            FILTER_DEAL_COUNT -> SORT_DEAL_COUNT
            FILTER_LATEST_DEAL -> SORT_LATEST_DEAL
            else -> SORT_STAR_RATING
        }
    }

    private fun navigateToProfileManagement(player: PlayerDto) {
        val profileDto = ProfileDto(
            user = ProfileDto.User(
                user_id = player.user_id.toInt(),
                player_id = player.player_id.toInt(),
                nickname = player.nickname,
                profile_image = player.profile_image,
                enable = player.enable
            ),
            evaluation = ProfileDto.Evaluation(
                start_average = player.star_average,
                cancel_count = 0
            ),
            requst_count = 0,
            review_count = player.complete_count
        )
        val action = SearchResultFragmentDirections.actionSearchResultFragmentToProfileManagementFragment(
            profileDto = profileDto,
            isPlayer = true,
            isFromSearchResult = true
        )
        findNavController().navigate(action)
    }

    companion object {
        private const val FILTER_DISTANCE = "거리순"
        private const val FILTER_STAR_RATING = "별점순"
        private const val FILTER_DEAL_COUNT = "거래건순"
        private const val FILTER_LATEST_DEAL = "최근 거래순"
        private val PLAYER_SEARCH_FILTERS = listOf(FILTER_STAR_RATING, FILTER_DEAL_COUNT, FILTER_LATEST_DEAL)

        private const val SORT_DISTANCE = "DISTANCE"
        private const val SORT_STAR_RATING = "STAR_RATING"
        private const val SORT_DEAL_COUNT = "DEAL_COUNT"
        private const val SORT_LATEST_DEAL = "LATEST_DEAL"
    }
}
