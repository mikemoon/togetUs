package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryMapBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryMapFragment : BaseFragment<FragmentDeliveryMapBinding, DeliveryMapViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_map
    override val viewModel: DeliveryMapViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                DeliveryMapViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                DeliveryMapViewModel.Event.SelectStart -> {
                    findNavController().navigate(R.id.action_deliveryMapFragment_to_deliveryStartFragment2)
                }
            }
        }
    }
}