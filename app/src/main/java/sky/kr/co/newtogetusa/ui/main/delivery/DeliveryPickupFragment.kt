package sky.kr.co.newtogetusa.ui.main.delivery

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryPickupBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomCalendarDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomTimeDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.toKoreanDateYYYYMMDDEEEE
import sky.kr.co.newtogetusa.utils.toYYYYMMDD
import kotlin.getValue

@AndroidEntryPoint
class DeliveryPickupFragment :
    BaseFragment<FragmentDeliveryPickupBinding, DeliveryPickupViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_pickup
    override val viewModel: DeliveryPickupViewModel by viewModels()

    private val sharedViewModel : DeliveryRequestSharedViewModel by navGraphViewModels(R.id.home)

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
        dataBinding.tvImmediate.apply {
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvImmediate)
                setSelectPickupType(false)
            }
        }

        dataBinding.tvIndicator2.post {
            val params = dataBinding.tvIndicator2.layoutParams
            params.width = dataBinding.tvMeet.width
            dataBinding.tvIndicator2.layoutParams = params
        }
        dataBinding.tvMeet.apply {
            isSelected = true
            setOnClickListener {
                moveIndicator2To(dataBinding.tvMeet)
                setSelectMeetType(true)
            }
        }
        dataBinding.tvNotMeet.apply {
            setOnClickListener {
                moveIndicator2To(dataBinding.tvNotMeet)
                setSelectMeetType(false)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectPickupType(isReservation: Boolean) {
        dataBinding.tvReservation.apply {
            isSelected = isReservation
            //background = if(isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if (isReservation) R.color.white else R.color.black_80))
        }
        dataBinding.tvImmediate.apply {
            isSelected = !isReservation
            //background = if(!isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if (!isReservation) R.color.white else R.color.black_80))
        }
        viewModel.isImmediately.value = !isReservation
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectMeetType(isMeet: Boolean) {
        dataBinding.tvMeet.apply {
            isSelected = isMeet
            setTextColor(requireContext().getColor(if (isMeet) R.color.white else R.color.black_80))
        }
        dataBinding.tvNotMeet.apply {
            isSelected = !isMeet
            setTextColor(requireContext().getColor(if (!isMeet) R.color.white else R.color.black_80))
        }
        viewModel.isFaceToFace.value = isMeet
    }

    private fun moveIndicatorTo(target: View) {
        val animator =
            ObjectAnimator.ofFloat(dataBinding.tvIndicator, "translationX", target.x - 4.dpToPx())
        animator.duration = 250
        animator.start()
    }

    private fun moveIndicator2To(target: View) {
        dataBinding.tvIndicator2.post {
            val params = dataBinding.tvIndicator2.layoutParams
            params.width = target.width
            dataBinding.tvIndicator2.layoutParams = params

            val animator = ObjectAnimator.ofFloat(
                dataBinding.tvIndicator2,
                "translationX",
                target.x - 4.dpToPx()
            )
            animator.duration = 250
            animator.start()
        }
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.tvConfirm.setOnClickListener {
            sharedViewModel.updatePickupInfo(
                isImmediately = viewModel.isImmediately.value,
                date = viewModel.pickupDate.value,
                time = viewModel.pickupTime.value,
                isFaceToFace = viewModel.isFaceToFace.value
            )
            findNavController().popBackStack()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryPickupViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                DeliveryPickupViewModel.Event.SelectDate -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomCalendarDialog().apply {
                            daySelectCallback = { selectedDate ->
                                this@DeliveryPickupFragment.dataBinding.etDate.setText(selectedDate.toKoreanDateYYYYMMDDEEEE())
                                this@DeliveryPickupFragment.viewModel.pickupDate.value = selectedDate.toYYYYMMDD()
                            }
                        }
                    )
                }

                DeliveryPickupViewModel.Event.SelectTime -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomTimeDialog().apply {
                            timeCallback = { hour24, minutes ->
                                if(hour24 == -1 && minutes == -1){ //즉시
                                    this@DeliveryPickupFragment.viewModel.pickupTime.value = "0000"
                                }else{
                                    this@DeliveryPickupFragment.dataBinding.etTime.setText(formatAmPm(hour24, minutes))
                                    this@DeliveryPickupFragment.viewModel.pickupTime.value = "${hour24}${minutes}"
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun formatAmPm(hour24: Int, minute: Int): String {
        val amPm = if (hour24 < 12) "오전" else "오후"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return "$amPm $hour12:${"%02d".format(minute)}"
    }
}