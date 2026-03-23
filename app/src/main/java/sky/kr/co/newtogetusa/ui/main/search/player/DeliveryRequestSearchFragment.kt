package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryRequestSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomDeliveryFilterDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomFilterDialog
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber

@AndroidEntryPoint
class DeliveryRequestSearchFragment : BaseFragment<FragmentDeliveryRequestSearchBinding, DeliveryRequestSearchViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_request_search
    override val viewModel: DeliveryRequestSearchViewModel by viewModels()

    private lateinit var deliveryRequestSearchAdapter: DeliveryRequestSearchAdapter

    override fun init() {
        super.init()

        deliveryRequestSearchAdapter = DeliveryRequestSearchAdapter()
        dataBinding.rv.apply {
            adapter = deliveryRequestSearchAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryRequestPagingData.collectLatest { pagingData ->
                    deliveryRequestSearchAdapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isModePlayer.collectLatest { isModePlayer ->
                    Timber.d("isModePlayer $isModePlayer")
                    if (isModePlayer == false && findNavController().currentDestination?.id == R.id.deliveryRequestSearchFragment) {
                        findNavController().navigate(R.id.action_deliveryRequestSearchFragment_to_searchFragment)
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                DeliveryRequestSearchViewModel.Event.Sort -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomFilterDialog().apply {
                            filterList = listOf("마감 임박순", "최신 등록순")
                            itemSelectCallback = { selectedItem ->
                                this@DeliveryRequestSearchFragment.dataBinding.tvSort.text = selectedItem
                                this@DeliveryRequestSearchFragment.viewModel.updateSortType(selectedItem.toSortType())
                            }
                        }
                    )
                }

                DeliveryRequestSearchViewModel.Event.Filter -> {
                    Timber.d("filter")
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomDeliveryFilterDialog().apply {
                            initialFilterOption = BottomDeliveryFilterDialog.FilterOption(
                                myArea = this@DeliveryRequestSearchFragment.viewModel.searchCondition.value.myArea,
                                face2Face = this@DeliveryRequestSearchFragment.viewModel.searchCondition.value.face2Face,
                                immediately = this@DeliveryRequestSearchFragment.viewModel.searchCondition.value.immediately
                            )
                            filterConfirmCallback = { option ->
                                this@DeliveryRequestSearchFragment.viewModel.updateFilterCondition(
                                    myArea = option.myArea,
                                    face2Face = option.face2Face,
                                    immediately = option.immediately
                                )
                            }
                        }
                    )
                }

                DeliveryRequestSearchViewModel.Event.AreaRequirement -> {
                    findNavController().navigate(DeliveryRequestSearchFragmentDirections.actionDeliveryRequestSearchFragmentToDeliveryAreaRequirementSetFragment())
                }
            }
        }

    }

    private fun String.toSortType(): String = when (this) {
        FILTER_DEADLINE -> DeliveryRequestSearchViewModel.SORT_TYPE_DEADLINE
        else -> DeliveryRequestSearchViewModel.SORT_TYPE_NEWEST
    }

    companion object {
        private const val FILTER_DEADLINE = "마감 임박순"
    }
}