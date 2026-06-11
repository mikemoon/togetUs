package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
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
import sky.kr.co.newtogetusa.utils.hideKeyboard
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class DeliveryStartFragment : BaseFragment<FragmentDeliveryStartBinding, DeliveryStartViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_start
    override val viewModel: DeliveryStartViewModel by viewModels()

    private val sharedViewModel : DeliveryRequestSharedViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var placesClient: PlacesClient
    //private lateinit var searchResultAdapter: DeliveryStartKakaoSearchResultAdapter
    private val args : DeliveryStartFragmentArgs by navArgs()

    private var isFirstEnter = true

    override fun init() {
        super.init()

        viewModel.isStart.value = args.isStart
        viewModel.isInternationalDelivery.value = args.isInternational

        /*args.selectedKakaoLocValue?.let {
            viewModel.setSelectedAddress(it)
            dataBinding.tvSearch.text = it.name
        }*/

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyA9ZdMta--H5FgxapDt2AyHV6ZooYBht54")
        }
        placesClient = Places.createClient(requireContext())

        //searchResultAdapter = DeliveryStartKakaoSearchResultAdapter(viewModel)

        //dataBinding.rvSearchResult.adapter = searchResultAdapter
        setupHideKeyboardOnOutsideTouch(dataBinding.rootContainer)

        dataBinding.tvSearch.setOnClickListener {
            findNavController().navigate(DeliveryStartFragmentDirections.actionDeliveryStartFragment2ToDeliverySearchFragment(isStart = viewModel.isStart.value, isInternational = viewModel.isInternationalDelivery.value))
        }
        if(!viewModel.isStart.value && isFirstEnter){
            isFirstEnter = false
            findNavController().navigate(DeliveryStartFragmentDirections.actionDeliveryStartFragment2ToDeliverySearchFragment(isStart = viewModel.isStart.value, isInternational = viewModel.isInternationalDelivery.value))
        }
    }

    @OptIn(FlowPreview::class)
    override fun initObserver() {
        super.initObserver()

        /*lifecycleScope.launch {
            viewModel.results.collectLatest { pagingData ->
                // Paging 데이터 붙이기
                searchResultAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }*/

        // 로딩/에러/빈 상태 처리(선택)
        /*lifecycleScope.launch {
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
        }*/

        /*viewModel.selectedAddress.observe(viewLifecycleOwner){
            val bundle = Bundle().apply {
                putBoolean("isStart", viewModel.isStart)
                putParcelable("selectedKakaoLocValue", it)
            }
            // 결과 전달
            parentFragmentManager.setFragmentResult("fromB", bundle)
            findNavController().popBackStack()
        }*/

        dataBinding.etName.doAfterTextChanged {
            viewModel.name.value = it?.toString().orEmpty()
        }

        dataBinding.etPhone.doAfterTextChanged {
            viewModel.phone.value = it?.toString().orEmpty()
        }

        dataBinding.etAddressDetail.doAfterTextChanged {
            viewModel.addressDetail.value = it?.toString().orEmpty()
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
            viewModel.setSelectedAddress(result)
            dataBinding.tvSearch.text = result.name
            Timber.d("kakaoLocSelected1, $result, ")
            /*result.let {
                val bundle = Bundle().apply {
                    putBoolean("isStart", viewModel.isStart)
                    putParcelable("selectedKakaoLocValue", it)
                }
                // 결과 전달
                parentFragmentManager.setFragmentResult("fromB", bundle)
            }
            findNavController().popBackStack()*/
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is DeliveryStartViewModel.Event.Back -> {
                    //setFragmentResult()
                    findNavController().popBackStack()
                }
                is DeliveryStartViewModel.Event.FindAddressFromMap ->{
                    findNavController().navigate(DeliveryStartFragmentDirections.actionDeliveryStartFragment2ToDeliveryAddressMapFragment(isStart = viewModel.isStart.value, isInternational = viewModel.isInternationalDelivery.value))
                }
                is DeliveryStartViewModel.Event.InputComplete ->{
                    setFragmentResult()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setFragmentResult(){
        if(viewModel.isStart.value){
            sharedViewModel.updateStartLocation(
                address = viewModel.selectedAddress.value?.name.orEmpty(),
                detail = dataBinding.etAddressDetail.text.toString(),
                lat = viewModel.selectedAddress.value?.lat ?: 0.0,
                lng = viewModel.selectedAddress.value?.lng ?:0.0
            )
        }else{
            sharedViewModel.updateDestinationLocation(
                address = viewModel.selectedAddress.value?.name.orEmpty(),
                detail = dataBinding.etAddressDetail.text.toString(),
                lat = viewModel.selectedAddress.value?.lat ?: 0.0,
                lng = viewModel.selectedAddress.value?.lng ?:0.0
            )
        }
        sharedViewModel.updateUser(viewModel.name.value, viewModel.phone.value)
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

    private fun setupHideKeyboardOnOutsideTouch(view: View) {
        if (view is EditText) return

        view.setOnTouchListener { _, _ ->
            clearInputFocusAndHideKeyboard()
            false
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupHideKeyboardOnOutsideTouch(view.getChildAt(i))
            }
        }
    }

    private fun clearInputFocusAndHideKeyboard() {
        val focusedView = requireActivity().currentFocus ?: view?.findFocus() ?: dataBinding.root
        focusedView.clearFocus()
        requireContext().hideKeyboard(focusedView)
    }

}
