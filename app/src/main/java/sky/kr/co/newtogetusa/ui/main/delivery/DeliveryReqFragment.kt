package sky.kr.co.newtogetusa.ui.main.delivery

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.request.delivery.ContactInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DepartInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.DestInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.PickupInfo
import sky.kr.co.newtogetusa.data.remote.request.delivery.ProductInfo
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryReqBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.AbroadGuideDialog
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class DeliveryReqFragment : BaseFragment<FragmentDeliveryReqBinding, DeliveryReqViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_req
    override val viewModel: DeliveryReqViewModel by viewModels()
    private val args: DeliveryReqFragmentArgs by navArgs()

    private val sharedViewModel : DeliveryRequestSharedViewModel by navGraphViewModels(R.id.nav_graph)

    override fun init() {
        super.init()

        dataBinding.tvIndicator.post {
            val params = dataBinding.tvIndicator.layoutParams
            params.width = dataBinding.tvReservation.width
            dataBinding.tvIndicator.layoutParams = params
        }

        dataBinding.tvReservation.apply {
            isSelected = true
            //setTextColor(requireContext().getColor(R.color.white))
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvReservation)
                setSelectPickupType(true)
            }
        }
        dataBinding.tvImmediate.apply { //해외
            setOnClickListener {
                viewModel.onAbroadDeliveryClick{ isAgree ->
                    Timber.d("isAgreeCallback : $isAgree")
                    if(!isAgree){
                        AbroadGuideDialog().apply {
                            isAgreeCallback = {
                                moveIndicatorTo(this@DeliveryReqFragment.dataBinding.tvImmediate)
                                setSelectPickupType(false)
                            }
                        }.show(childFragmentManager, "")
                    }else{
                        moveIndicatorTo(dataBinding.tvImmediate)
                        setSelectPickupType(false)
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                sharedViewModel.state.collect { state ->
                    val hasLocationSummary = !state.startAddress.isNullOrBlank() && !state.destinationAddress.isNullOrBlank()
                    dataBinding.layoutLocationSummary.isVisible = hasLocationSummary
                    dataBinding.ivLocation.setImageResource(
                        if (hasLocationSummary) R.drawable.pin_fill_primary else R.drawable.pin_fill_gray
                    )
                    if (hasLocationSummary) {
                        dataBinding.tvStartAddress.text = state.startAddress.orEmpty()
                        dataBinding.tvDestinationAddress.text = state.destinationAddress.orEmpty()
                    }

                    val hasPickupSummary = !state.pickupDate.isNullOrBlank() && !state.pickupTime.isNullOrBlank()
                    dataBinding.layoutDateSummary.isVisible = hasPickupSummary
                    dataBinding.ivDate.setImageResource(
                        if (hasPickupSummary) R.drawable.calendar_fill_primary else R.drawable.calendar_fill_gray
                    )
                    if (hasPickupSummary) {
                        dataBinding.tvPickupDateSummary.text = "${formatKoreanDate(state.pickupDate.orEmpty())} ${formatTimeToKorean(state.pickupTime.orEmpty())}"
                        dataBinding.tvPickupMethodSummary.text = "픽업 전달 방식  ${if (state.pickupIsFaceToFace) "대면" else "비대면"}"
                    }

                    val hasProductSummary = !state.productTitle.isNullOrBlank() && !state.productType.isNullOrBlank() && !state.productWeight.isNullOrBlank() && !state.productVolume.isNullOrBlank()
                    dataBinding.layoutProductSummary.isVisible = hasProductSummary
                    dataBinding.ivProduct.setImageResource(
                        if (hasProductSummary) R.drawable.box_fill_primary else R.drawable.box_fill_gray
                    )
                    if (hasProductSummary) {
                        dataBinding.tvProductTitleSummary.text = state.productTitle.orEmpty()
                        dataBinding.tvProductTypeSummary.text =
                            state.productTypeLabel?.takeIf { it.isNotBlank() } ?: state.productType.orEmpty()
                        dataBinding.tvProductWeightSummary.text =
                            state.productWeightLabel?.takeIf { it.isNotBlank() } ?: state.productWeight.orEmpty()
                        dataBinding.tvProductVolumeSummary.text =
                            state.productVolumeLabel?.takeIf { it.isNotBlank() } ?: state.productVolume.orEmpty()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.isFeeConfirmReady.collect { ready ->
                    viewModel.updateChargeEnable(ready)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                DeliveryReqViewModel.Event.Back ->{
                    handleBack()
                }
                DeliveryReqViewModel.Event.Charge ->{
                    if (args.isEdit && args.deliveryId > 0L) {
                        viewModel.editDelivery(args.deliveryId, sharedViewModel.state.value.toDeliveryRequest())
                    } else {
                        findNavController().navigate(DeliveryReqFragmentDirections.actionDeliveryReqFragmentToDeliveryFeeFragment())
                    }
                }
                DeliveryReqViewModel.Event.StartLocation ->{
                    findNavController().navigate(DeliveryReqFragmentDirections.actionDeliveryReqFragmentToDeliveryMapFragment(isInternational = viewModel.isInternationalDelivery.value))
                }
                DeliveryReqViewModel.Event.PickupDate ->{
                    findNavController().navigate(R.id.action_deliveryReqFragment_to_deliveryPickupFragment)
                }
                DeliveryReqViewModel.Event.ProductInfo ->{
                    findNavController().navigate(R.id.action_deliveryReqFragment_to_deliveryProductFragment)
                }
                DeliveryReqViewModel.Event.EditCompleted -> {
                    sharedViewModel.clearState()
                    MessageDialog.newInstance(
                        msg = "수정되었습니다.",
                        rightBtn = "확인"
                    ).onRightBtn {
                        handleBack(clearState = false)
                    }.show(childFragmentManager, "MessageDialog")
                }
                is DeliveryReqViewModel.Event.ShowMessage -> {
                    MessageDialog.newInstance(
                        msg = event.message,
                        rightBtn = "확인"
                    ).show(childFragmentManager, "MessageDialog")
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collect { show ->
                    if (show) showLoading() else hideLoading()
                }
            }
        }
    }

    private fun handleBack(clearState: Boolean = true) {
        if (clearState) {
            sharedViewModel.clearState()
        }
        if (args.returnToDetail) {
            val popped = findNavController().popBackStack(R.id.historyDetailFragment, false) ||
                findNavController().popBackStack(R.id.playerHistoryDetailFragment, false)
            if (!popped) {
                findNavController().popBackStack()
            }
        } else if (args.returnToHistory) {
            val popped = findNavController().popBackStack(R.id.historyFragment, false)
            if (!popped) {
                (requireActivity() as MainActivity).selectMainTab(R.id.history)
            }
        } else {
            findNavController().popBackStack()
        }
    }

    private fun DeliveryRequestState.toDeliveryRequest(): DeliveryRequest =
        DeliveryRequest(
            title = productTitle.orEmpty(),
            is_domestic = !isInternational,
            depart = DepartInfo(
                address = startAddress.orEmpty(),
                address2 = startDetail.orEmpty(),
                latitude = startLat ?: 0.0,
                longitude = startLng ?: 0.0,
            ),
            depart_contact = ContactInfo(
                name = name,
                phone = phone
            ),
            dest = DestInfo(
                address = destinationAddress.orEmpty(),
                address2 = destinationDetail.orEmpty(),
                latitude = destLat ?: 0.0,
                longitude = destLng ?: 0.0,
            ),
            dest_contact = ContactInfo(
                name = null,
                phone = null
            ),
            pickup = PickupInfo(
                is_immediately = pickupIsImmediately,
                date = pickupDate.orEmpty(),
                time = pickupTime.orEmpty(),
                is_face2face = pickupIsFaceToFace
            ),
            product = ProductInfo(
                name = productTitle.orEmpty(),
                type_cd = productType.orEmpty(),
                weight_cd = productWeight.orEmpty(),
                volume_cd = productVolume.orEmpty(),
                descript = productDescription.orEmpty()
            )
        )

    private fun moveIndicatorTo(target: View) {
        val animator = ObjectAnimator.ofFloat(dataBinding.tvIndicator, "translationX", target.x - 4.dpToPx())
        animator.duration = 250
        animator.start()
    }

    private fun formatKoreanDate(input: String): String {
        return runCatching {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
            LocalDate.parse(input, inputFormatter).format(outputFormatter)
        }.getOrElse { input }
    }

    private fun formatTimeToKorean(time: String): String {
        return runCatching {
            val inputFormatter = DateTimeFormatter.ofPattern("HHmm")
            val outputFormatter = DateTimeFormatter.ofPattern("a h시", Locale.KOREAN)
            val normalized = time.padStart(4, '0').takeLast(4)
            LocalTime.parse(normalized, inputFormatter).format(outputFormatter)
        }.getOrElse { time }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectPickupType(isReservation: Boolean){
        viewModel.isInternationalDelivery.value = !isReservation
        dataBinding.tvReservation.apply {
            isSelected = isReservation
            //background = if(isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if(isReservation) R.color.white else R.color.black_80))
        }
        dataBinding.tvImmediate.apply {
            isSelected = !isReservation
            //background = if(!isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if(!isReservation) R.color.white else R.color.black_80))
        }
    }
}
