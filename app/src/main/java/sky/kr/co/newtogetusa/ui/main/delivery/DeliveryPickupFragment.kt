package sky.kr.co.newtogetusa.ui.main.delivery

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryPickupBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomCalendarDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomTimeDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class DeliveryPickupFragment : BaseFragment<FragmentDeliveryPickupBinding, DeliveryPickupViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_pickup
    override val viewModel: DeliveryPickupViewModel by viewModels()

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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectPickupType(isReservation: Boolean){
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

    private fun moveIndicatorTo(target: View) {
        val animator = ObjectAnimator.ofFloat(dataBinding.tvIndicator, "translationX", target.x - 4.dpToPx())
        animator.duration = 250
        animator.start()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                DeliveryPickupViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                DeliveryPickupViewModel.Event.SelectDate -> {
                    dialogFragmentShow(childFragmentManager,
                        BottomCalendarDialog()
                        )
                }
                DeliveryPickupViewModel.Event.SelectTime ->{
                    dialogFragmentShow(childFragmentManager,
                        BottomTimeDialog()
                    )
                }
            }
        }
    }
}