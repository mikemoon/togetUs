package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryFeeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import kotlin.getValue

@AndroidEntryPoint
class DeliveryFeeFragment : BaseFragment<FragmentDeliveryFeeBinding, DeliveryFeeVM>() {

    override val layoutId: Int
        get() = R.layout.fragment_delivery_fee

    override val viewModel: DeliveryFeeVM by viewModels()

    private val sharedViewModel : DeliveryRequestSharedViewModel by navGraphViewModels(R.id.home)


    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                is DeliveryFeeVM.Event.Back ->{
                    findNavController().popBackStack()
                }
            }
        }
    }

}