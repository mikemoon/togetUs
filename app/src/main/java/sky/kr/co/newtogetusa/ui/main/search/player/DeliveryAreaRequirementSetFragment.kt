package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryAreaRequirementSetBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryAreaRequirementSetFragment : BaseFragment<FragmentDeliveryAreaRequirementSetBinding, DeliveryAreaRequirementSetViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_area_requirement_set
    override val viewModel: DeliveryAreaRequirementSetViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                DeliveryAreaRequirementSetViewModel.Event.Close ->{
                    findNavController().popBackStack()
                }
            }
        }
    }
}