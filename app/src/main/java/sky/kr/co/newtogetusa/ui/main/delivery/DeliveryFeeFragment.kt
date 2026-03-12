package sky.kr.co.newtogetusa.ui.main.delivery

import android.annotation.SuppressLint
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.request.delivery.ContactInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DepartInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.DestInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.PickupInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.ProductInfo
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryFeeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.ImageUtil
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class DeliveryFeeFragment : BaseFragment<FragmentDeliveryFeeBinding, DeliveryFeeVM>() {

    override val layoutId: Int
        get() = R.layout.fragment_delivery_fee

    override val viewModel: DeliveryFeeVM by viewModels()

    private val sharedViewModel: DeliveryRequestSharedViewModel by navGraphViewModels(R.id.nav_graph)


    override fun init() {
        super.init()


        val deliveryReqInfo = sharedViewModel.state.value
        deliveryReqInfo?.let { dr ->
            viewModel.createDelivery(
                DeliveryRequest(
                    title = dr.productTitle.orEmpty(),
                    is_domestic = dr.isInternational == true,
                    depart = DepartInfo(
                        address = dr.startAddress.orEmpty(),
                        address2 = dr.startDetail.orEmpty(),
                        latitude = dr.startLat?:0.0,
                        longitude = dr.startLng?:0.0,
                    ),
                    depart_contact = ContactInfo(
                        name = dr.name,
                        phone = dr.phone
                    ),
                    dest = DestInfo(
                        address = dr.destinationAddress.orEmpty(),
                        address2 = dr.destinationDetail.orEmpty(),
                        latitude = dr.destLat?:0.0,
                        longitude = dr.destLng?:0.0,
                    ),
                    dest_contact = ContactInfo(
                        name = null,
                        phone = null
                    ),
                    pickup = PickupInfo(
                        is_immediately = dr.pickupIsImmediately,
                        date = dr.pickupDate.orEmpty(),
                        time = dr.pickupTime.orEmpty(),
                        is_face2face = dr.pickupIsFaceToFace
                    ),
                    product = ProductInfo(
                        name = dr.productTitle.orEmpty(),
                        type_cd = dr.productType.orEmpty(),
                        weight_cd = dr.productWeight.orEmpty(),
                        volume_cd = dr.productVolume.orEmpty(),
                        descript = dr.productDescription.orEmpty()
                    )
                )
            ){ result ->
                viewModel.checkingFee(result)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    override fun initObserver() {
        super.initObserver()


        dataBinding.etAdjustFee.doAfterTextChanged { text ->

        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.state.collect { state ->
                    val distanceText = state.distanceKm
                        .toDoubleOrNull()
                        ?.let { String.format("%.2f km", it) }
                        ?: "0 km"
                    viewModel.setDistance(distanceText)
                    viewModel.setProductWeight(when(state.productWeight){
                        else -> "가벼움(~3KG)"
                    })
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryUiModel.collect { uiModel ->
                    uiModel?.let {
                        dataBinding.uiModel = it
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is DeliveryFeeVM.Event.Back -> {
                    findNavController().popBackStack()
                }
                is DeliveryFeeVM.Event.RegisterDelivery ->{
                    val registerDeliveryAction = {
                        viewModel.registerDelivery(
                            req = DeliveryFinalReq(
                                fee_adjust = dataBinding.etAdjustFee.text.toString().toLongOrNull() ?: 0
                            )
                        ) { ret ->
                            if (ret) {
                                sharedViewModel.clearState()
                                MessageDialog.newInstance(
                                    msg = "동행 요청 등록이 완료되었어요.",
                                    rightBtn = "확인"
                                ).onRightBtn {
                                    findNavController().navigate(
                                        R.id.homeTabFragment,
                                        null,
                                        NavOptions.Builder()
                                            .setPopUpTo(R.id.homeTabFragment, true)
                                            .build()
                                    )
                                }.show(childFragmentManager, "MessageDialog")
                            }
                        }
                    }

                    val uris = sharedViewModel.attachImagesUrl.value

                    if (uris.isNotEmpty()) {

                        val photos = ImageUtil.uriListToPhotos(
                            requireContext(),
                            uris,
                            maxSize = 1024
                        )

                        viewModel.registerPhoto(photos) {
                            registerDeliveryAction()
                        }

                    } else {
                        registerDeliveryAction()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.loadingState.collect { show ->
                    if(show)showLoading() else hideLoading()
                }
            }
        }

    }

}