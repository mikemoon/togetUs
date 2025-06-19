package sky.kr.co.newtogetusa.ui.main.delivery

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
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
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryStartBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber

@AndroidEntryPoint
class DeliveryStartFragment : BaseFragment<FragmentDeliveryStartBinding, DeliveryStartViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_start
    override val viewModel: DeliveryStartViewModel by viewModels()

    private lateinit var placesClient: PlacesClient
    private lateinit var searchResultAdapter: DeliveryStartSearchResultAdapter

    override fun init() {
        super.init()

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyA9ZdMta--H5FgxapDt2AyHV6ZooYBht54")
        }
        placesClient = Places.createClient(requireContext())

        searchResultAdapter = DeliveryStartSearchResultAdapter(viewModel)

        dataBinding.rvSearchResult.adapter = searchResultAdapter
    }

    @OptIn(FlowPreview::class)
    override fun initObserver() {
        super.initObserver()

        viewModel.selectedAddress.observe(viewLifecycleOwner){
        }

        lifecycleScope.launch {
            viewModel.searchAddress.debounce(400).filter { it.length > 1 }.distinctUntilChanged().collectLatest { searchText ->
                Timber.d("collect searchText : $searchText")

                fetchAutocompleteSuggestions(searchText)

            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is DeliveryStartViewModel.Event.Back -> {
                    viewModel.selectedAddress.value?.let {
                        val bundle = Bundle().apply {
                            putParcelable("selectedValue", it)
                        }
                        // 결과 전달
                        parentFragmentManager.setFragmentResult("fromB", bundle)
                    }

                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun fetchAutocompleteSuggestions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountry("KR")
            .build()

        placesClient.findAutocompletePredictions(request)
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
            }
    }

}