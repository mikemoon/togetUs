package sky.kr.co.newtogetusa.ui.main.delivery

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryPickupBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomCalendarDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class DeliveryPickupFragment : BaseFragment<FragmentDeliveryPickupBinding, DeliveryPickupViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_pickup
    override val viewModel: DeliveryPickupViewModel by viewModels()

    override fun init() {
        super.init()

        dataBinding.tvReservation.apply {
            isSelected = true
            setTextColor(requireContext().getColor(R.color.white))
            setOnClickListener {
                setSelectPickupType(true)
            }
        }
        dataBinding.tvImmediate.apply {
            setOnClickListener {
                setSelectPickupType(false)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectPickupType(isReservation: Boolean){
        dataBinding.tvReservation.apply {
            isSelected = isReservation
            background = if(isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if(isReservation) R.color.white else R.color.black_80))
        }
        dataBinding.tvImmediate.apply {
            isSelected = !isReservation
            background = if(!isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if(!isReservation) R.color.white else R.color.black_80))
        }
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

                }
            }
        }
    }
}