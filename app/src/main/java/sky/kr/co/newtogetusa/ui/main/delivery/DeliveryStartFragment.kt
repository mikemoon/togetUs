package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
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

    private val sharedViewModel : DeliveryRequestSharedViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private val args : DeliveryStartFragmentArgs by navArgs()

    override fun init() {
        super.init()

        viewModel.isStart.value = args.isStart
        viewModel.isInternationalDelivery.value = args.isInternational

        applySavedLocation()
        applySavedContact()
        setupHideKeyboardOnOutsideTouch(dataBinding.rootContainer)

        dataBinding.tvSearch.setOnClickListener {
            findNavController().navigate(DeliveryStartFragmentDirections.actionDeliveryStartFragment2ToDeliverySearchFragment(isStart = viewModel.isStart.value, isInternational = viewModel.isInternationalDelivery.value))
        }
    }

    private fun applySavedLocation() {
        val state = sharedViewModel.state.value
        val address = if (viewModel.isStart.value) state.startAddress else state.destinationAddress
        val detail = if (viewModel.isStart.value) state.startDetail else state.destinationDetail
        val lat = if (viewModel.isStart.value) state.startLat else state.destLat
        val lng = if (viewModel.isStart.value) state.startLng else state.destLng

        if (address.isNullOrBlank() || lat == null || lng == null) return

        val selected = KakaoSearchModel(
            name = address,
            lat = lat,
            lng = lng,
            subtitle = address,
            distance = null,
            roadAddress = address,
            source = "CURRENT"
        )
        applySelectedAddress(selected, persist = false)
        viewModel.addressDetail.value = detail.orEmpty()
        dataBinding.etAddressDetail.setText(detail.orEmpty())
    }

    private fun applySavedContact() {
        val state = sharedViewModel.state.value
        val savedName = state.name.orEmpty()
        val savedPhone = state.phone.orEmpty()

        viewModel.name.value = savedName
        viewModel.phone.value = savedPhone
        dataBinding.etName.setText(savedName)
        dataBinding.etPhone.setText(savedPhone)
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.etName.doAfterTextChanged {
            viewModel.name.value = it?.toString().orEmpty()
            updateSavedContact()
        }

        dataBinding.etPhone.doAfterTextChanged {
            viewModel.phone.value = it?.toString().orEmpty()
            updateSavedContact()
        }

        dataBinding.etAddressDetail.doAfterTextChanged {
            viewModel.addressDetail.value = it?.toString().orEmpty()
            persistSelectedLocation()
        }

        parentFragmentManager.setFragmentResultListener("fromC", viewLifecycleOwner) { requestKey, bundle ->
            Timber.d("kakaoLocSelected1")
            val result = bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                ?: return@setFragmentResultListener
            applySelectedAddress(result)
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
        persistSelectedLocation()
        sharedViewModel.updateUser(viewModel.name.value, viewModel.phone.value)
    }

    private fun applySelectedAddress(
        selectedAddress: KakaoSearchModel,
        persist: Boolean = true
    ) {
        viewModel.setSelectedAddress(selectedAddress)
        dataBinding.tvSearch.text = selectedAddress.displayAddress()
        if (persist) {
            persistSelectedLocation()
        }
    }

    private fun persistSelectedLocation(): Boolean {
        val selectedAddress = viewModel.selectedAddress.value
        val address = selectedAddress.displayAddress()
        val lat = selectedAddress?.lat
        val lng = selectedAddress?.lng
        if (address.isBlank() || lat == null || lng == null) return false

        if(viewModel.isStart.value){
            sharedViewModel.updateStartLocation(
                address = address,
                detail = dataBinding.etAddressDetail.text.toString(),
                lat = lat,
                lng = lng
            )
        }else{
            sharedViewModel.updateDestinationLocation(
                address = address,
                detail = dataBinding.etAddressDetail.text.toString(),
                lat = lat,
                lng = lng
            )
        }
        return true
    }

    private fun KakaoSearchModel?.displayAddress(): String =
        this?.roadAddress?.takeIf { it.isNotBlank() }
            ?: this?.name?.takeIf { it.isNotBlank() }
            ?: this?.subtitle?.takeIf { it.isNotBlank() }
            ?: ""

    private fun updateSavedContact() {
        sharedViewModel.updateUser(viewModel.name.value, viewModel.phone.value)
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
