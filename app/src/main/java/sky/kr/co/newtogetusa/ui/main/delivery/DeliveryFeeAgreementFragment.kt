package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryFeeAgreementBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryFeeAgreementFragment :
    BaseFragment<FragmentDeliveryFeeAgreementBinding, DeliveryFeeAgreementViewModel>() {

    override val layoutId: Int = R.layout.fragment_delivery_fee_agreement
    override val viewModel: DeliveryFeeAgreementViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryFeeAgreementViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryFeeAgreementViewModel.Event.Confirm -> findNavController().popBackStack()
            }
        }
    }
}
