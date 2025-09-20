package sky.kr.co.newtogetusa.ui.main.delivery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryStartBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber

@AndroidEntryPoint
class DeliveryStartFragment : BaseFragment<FragmentDeliveryStartBinding, DeliveryStartViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_start
    override val viewModel: DeliveryStartViewModel by viewModels()

    private lateinit var placesClient: PlacesClient
    private lateinit var searchResultAdapter: DeliveryStartKakaoSearchResultAdapter

    override fun init() {
        super.init()

        viewModel.isStart = arguments?.getBoolean("isStart") == true

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyA9ZdMta--H5FgxapDt2AyHV6ZooYBht54")
        }
        placesClient = Places.createClient(requireContext())

        searchResultAdapter = DeliveryStartKakaoSearchResultAdapter(viewModel)

        dataBinding.rvSearchResult.adapter = searchResultAdapter
    }

    @OptIn(FlowPreview::class)
    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            viewModel.results.collectLatest { pagingData ->
                // Paging 데이터 붙이기
                searchResultAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }

        // 로딩/에러/빈 상태 처리(선택)
        lifecycleScope.launch {
            searchResultAdapter.loadStateFlow.collectLatest { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error
                val isEmpty = loadStates.refresh is LoadState.NotLoading &&
                        searchResultAdapter.itemCount == 0

                //dataBinding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
                //dataBinding.rvSearchResult.visibility = if (!isLoading && !isEmpty) View.VISIBLE else View.GONE
                //dataBinding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE

                if (isError) {
                    val e = (loadStates.refresh as LoadState.Error).error
                    Timber.e(e, "주소 검색 실패")
                }
            }
        }

        viewModel.selectedAddress.observe(viewLifecycleOwner){
            val bundle = Bundle().apply {
                putBoolean("isStart", viewModel.isStart)
                putParcelable("selectedKakaoLocValue", it)
            }
            // 결과 전달
            parentFragmentManager.setFragmentResult("fromB", bundle)
            findNavController().popBackStack()
        }

        lifecycleScope.launch {
            viewModel.searchAddress.debounce(400).filter { it.length > 1 }.distinctUntilChanged().collectLatest { searchText ->
                Timber.d("collect searchText : $searchText")

                fetchAutocompleteSuggestions(searchText)

            }
        }

        parentFragmentManager.setFragmentResultListener("fromC", viewLifecycleOwner) { requestKey, bundle ->
            Timber.d("kakaoLocSelected1")
            val result = bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                ?: return@setFragmentResultListener
            Timber.d("kakaoLocSelected1, $result, ")
            result.let {
                val bundle = Bundle().apply {
                    putBoolean("isStart", viewModel.isStart)
                    putParcelable("selectedKakaoLocValue", it)
                }
                // 결과 전달
                parentFragmentManager.setFragmentResult("fromB", bundle)
            }
            findNavController().popBackStack()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is DeliveryStartViewModel.Event.Back -> {
                    viewModel.selectedAddress.value?.let {
                        val bundle = Bundle().apply {
                            putBoolean("isStart", viewModel.isStart)
                            putParcelable("selectedKakaoLocValue", it)
                        }
                        // 결과 전달
                        parentFragmentManager.setFragmentResult("fromB", bundle)
                    }

                    findNavController().popBackStack()
                }
                is DeliveryStartViewModel.Event.FindAddressFromMap ->{
                    findNavController().navigate(DeliveryStartFragmentDirections.actionDeliveryStartFragment2ToDeliveryAddressMapFragment(isStart = viewModel.isStart))
                }
            }
        }
    }

    private fun fetchAutocompleteSuggestions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountry("KR")
            .build()

        /*placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val searchResultList : List<SearchResultModel> =
                    response.autocompletePredictions.map { SearchResultModel(placeId = it.placeId, placeName = it.getFullText(null).toString()) }
                Timber.d("Autocomplete results : $searchResultList")
                searchResultAdapter.updateList(searchResultList)
                dataBinding.rvSearchResult.visibility =
                    if (searchResultList.isNotEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Timber.d("Autocomplete failed: ${e.message}")
            }*/
    }

}