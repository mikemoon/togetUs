package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryReqBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryReqFragment : BaseFragment<FragmentDeliveryReqBinding, DeliveryReqViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_req
    override val viewModel: DeliveryReqViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                DeliveryReqViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                DeliveryReqViewModel.Event.StartLocation ->{
                    findNavController().navigate(R.id.action_deliveryReqFragment_to_deliveryMapFragment)
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
}