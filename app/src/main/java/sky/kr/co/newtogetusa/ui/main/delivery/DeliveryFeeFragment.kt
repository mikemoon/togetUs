package sky.kr.co.newtogetusa.ui.main.delivery

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
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
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class DeliveryFeeFragment : BaseFragment<FragmentDeliveryFeeBinding, DeliveryFeeVM>() {

    override val layoutId: Int
        get() = R.layout.fragment_delivery_fee

    override val viewModel: DeliveryFeeVM by viewModels()

    private val sharedViewModel: DeliveryRequestSharedViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private var isEditMode = false
    private var editDeliveryId = -1L
    private var hasAppliedInitialAdjustFee = false
    private var baseFee = 0L

    override fun init() {
        super.init()

        isEditMode = arguments?.getBoolean("isEdit") == true
        editDeliveryId = arguments?.getLong("deliveryId") ?: -1L
        setupModeUi()

        if (isEditMode) {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        handleBack()
                    }
                }
            )
            if (editDeliveryId > 0L) {
                viewModel.loadDeliveryFeeForEdit(editDeliveryId)
            }
            return
        }

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
            updateAdjustFeeClearVisibility()
            updateTotalFee()
            if (isEditMode) {
                updateEditSubmitState()
            }
        }

        dataBinding.ivAdjustFeeClear.setOnClickListener {
            dataBinding.etAdjustFee.text?.clear()
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
                viewModel.isAgreeChecked.collect {
                    if (isEditMode) updateEditSubmitState()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryUiModel.collect { uiModel ->
                    uiModel?.let {
                        dataBinding.uiModel = it
                        baseFee = it.baseFee
                        if (isEditMode && !hasAppliedInitialAdjustFee) {
                            dataBinding.etAdjustFee.setText(it.adjustFee)
                            dataBinding.etAdjustFee.setSelection(dataBinding.etAdjustFee.text?.length ?: 0)
                            hasAppliedInitialAdjustFee = true
                        }
                        updateTotalFee()
                        if (isEditMode) updateEditSubmitState()
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is DeliveryFeeVM.Event.Back -> {
                    handleBack()
                }
                is DeliveryFeeVM.Event.RegisterDelivery ->{
                    if (isEditMode) {
                        viewModel.registerDelivery(
                            req = DeliveryFinalReq(
                                fee_adjust = parseAdjustFee()
                            )
                        ) { ret ->
                            if (ret) {
                                MessageDialog.newInstance(
                                    msg = "수정되었습니다.",
                                    rightBtn = "확인"
                                ).onRightBtn {
                                    handleBack()
                                }.show(childFragmentManager, "MessageDialog")
                            }
                        }
                        return@observe
                    }

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

                        viewModel.registerPhoto(photos) { uploaded ->
                            sharedViewModel.clearAttachImages()
                            if (uploaded) {
                                registerDeliveryAction()
                            } else {
                                requireContext().toast("물품 사진 등록에 실패했습니다.")
                            }
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

    private fun setupModeUi() = with(dataBinding) {
        if (!isEditMode) return@with

        llAdjustFeeInput.setBackgroundResource(R.drawable.background_st_p60_s_p10_r4)
        etAdjustFee.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        etAdjustFee.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        tvRegister.text = "동행 요청 수정"
        updateAdjustFeeClearVisibility()
        this@DeliveryFeeFragment.viewModel.setRegisterButtonEnabled(false)
    }

    private fun updateEditSubmitState() = with(dataBinding) {
        val priceText = etAdjustFee.text?.toString()
            ?.replace(",", "")
            ?.trim()
            .orEmpty()
        val isValid = priceText.isEmpty() || priceText.toLongOrNull()?.let {
            it >= 1000L && it % 1000L == 0L
        } == true

        this@DeliveryFeeFragment.viewModel.setRegisterButtonEnabled(
            isValid && this@DeliveryFeeFragment.viewModel.isAgreeChecked.value
        )
        llAdjustFeeInput.setBackgroundResource(
            if (isValid) R.drawable.background_st_p60_s_p10_r4
            else R.drawable.background_st_primary100_s_primary10_r4
        )
    }

    private fun updateAdjustFeeClearVisibility() = with(dataBinding) {
        ivAdjustFeeClear.visibility =
            if (etAdjustFee.text?.isNotEmpty() == true) View.VISIBLE else View.GONE
    }

    private fun updateTotalFee() {
        val adjustFee = dataBinding.etAdjustFee.text?.toString()
            ?.replace(",", "")
            ?.trim()
            ?.toLongOrNull()
            ?: 0L
        dataBinding.tvTotalFee.text = "%,d원".format(baseFee + adjustFee)
    }

    private fun parseAdjustFee(): Long =
        dataBinding.etAdjustFee.text?.toString()
            ?.replace(",", "")
            ?.trim()
            ?.toLongOrNull()
            ?: 0L

    private fun handleBack() {
        if (!isEditMode) {
            findNavController().popBackStack()
            return
        }

        val navController = findNavController()
        val returnedToUserDetail = navController.popBackStack(R.id.historyDetailFragment, false)
        if (!returnedToUserDetail) {
            val returnedToPlayerDetail = navController.popBackStack(R.id.playerHistoryDetailFragment, false)
            if (!returnedToPlayerDetail) {
                navController.popBackStack()
            }
        }
    }

}
