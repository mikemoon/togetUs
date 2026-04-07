package sky.kr.co.newtogetusa.ui.main.search.player

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryAreaLocationSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartKakaoSearchResultAdapter

@AndroidEntryPoint
class DeliveryAreaLocationSearchFragment :
    BaseFragment<FragmentDeliveryAreaLocationSearchBinding, DeliveryAreaLocationSearchViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_delivery_area_location_search
    override val viewModel: DeliveryAreaLocationSearchViewModel by viewModels()

    private val args: DeliveryAreaLocationSearchFragmentArgs by navArgs()

    private lateinit var searchResultAdapter: DeliveryStartKakaoSearchResultAdapter

    override fun init() {
        super.init()

        searchResultAdapter = DeliveryStartKakaoSearchResultAdapter { model ->
            viewModel.onAddressSelected(model)
            dataBinding.tvSearchResult.text = model.roadAddress?.takeIf { it.isNotBlank() } ?: model.name
        }

        dataBinding.rv.adapter = searchResultAdapter
        dataBinding.slide.addOnChangeListener { _, value, _ ->
            viewModel.setAreaRadius(value)
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.results.collectLatest { pagingData ->
                        searchResultAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                    }
                }

                launch {
                    searchResultAdapter.loadStateFlow.collectLatest { loadStates ->
                        val isEmpty = loadStates.refresh is androidx.paging.LoadState.NotLoading &&
                                searchResultAdapter.itemCount == 0
                        if (viewModel.query.value.length < 2) {
                            viewModel.searchStep.value = DeliveryAreaLocationSearchViewModel.SearchStep.NONE
                        } else {
                            viewModel.searchStep.value = if (isEmpty) {
                                DeliveryAreaLocationSearchViewModel.SearchStep.NONE
                            } else {
                                DeliveryAreaLocationSearchViewModel.SearchStep.SEARCH
                            }
                        }
                    }
                }

                launch {
                    viewModel.searchStep.collectLatest { step ->
                        dataBinding.llSearch.isVisible = step == DeliveryAreaLocationSearchViewModel.SearchStep.SEARCH
                        dataBinding.clAreaSet.isVisible = step == DeliveryAreaLocationSearchViewModel.SearchStep.AREA_SET
                    }
                }

                launch {
                    viewModel.isEditMode.collectLatest { isEditMode ->
                        dataBinding.clEdit.isVisible = isEditMode
                        dataBinding.tvSearchResult.isVisible = !isEditMode
                        dataBinding.tvModify.isVisible = !isEditMode
                    }
                }

                launch {
                    viewModel.query.collectLatest { query ->
                        if (dataBinding.etSearch.text?.toString().orEmpty() != query) {
                            dataBinding.etSearch.setText(query)
                            dataBinding.etSearch.setSelection(query.length)
                        }
                        dataBinding.ivDelete.isVisible = query.isNotBlank()
                    }
                }

                launch {
                    viewModel.areaRadius.collectLatest { km ->
                        dataBinding.tvAreaDistance.text = "${km}km"
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryAreaLocationSearchViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryAreaLocationSearchViewModel.Event.SelectedComplete -> {
                    val selected = viewModel.selectedAddress.value ?: return@observe
                    val address = selected.roadAddress?.takeIf { it.isNotBlank() } ?: selected.name
                    val result = DeliveryAreaSelection(
                        name = address,
                        radiusKm = viewModel.areaRadius.value,
                        lat = selected.lat,
                        lng = selected.lng
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        if (args.isDeparture) KEY_DEPART_RESULT else KEY_DEST_RESULT,
                        result
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }

    companion object {
        const val KEY_DEPART_RESULT = "delivery_area_depart_result"
        const val KEY_DEST_RESULT = "delivery_area_dest_result"
    }
}