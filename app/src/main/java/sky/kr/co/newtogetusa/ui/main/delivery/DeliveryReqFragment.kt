package sky.kr.co.newtogetusa.ui.main.delivery

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryReqBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.AbroadGuideDialog
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber

@AndroidEntryPoint
class DeliveryReqFragment : BaseFragment<FragmentDeliveryReqBinding, DeliveryReqViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_req
    override val viewModel: DeliveryReqViewModel by viewModels()

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
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                DeliveryReqViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                DeliveryReqViewModel.Event.Charge ->{

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
            }
        }
    }

    private fun moveIndicatorTo(target: View) {
        val animator = ObjectAnimator.ofFloat(dataBinding.tvIndicator, "translationX", target.x - 4.dpToPx())
        animator.duration = 250
        animator.start()
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