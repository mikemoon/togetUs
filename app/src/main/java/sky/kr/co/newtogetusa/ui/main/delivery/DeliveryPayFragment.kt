package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryPayBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryPayFragment : BaseFragment<FragmentDeliveryPayBinding, DeliveryPayViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_pay
    override val viewModel: DeliveryPayViewModel by viewModels()

}