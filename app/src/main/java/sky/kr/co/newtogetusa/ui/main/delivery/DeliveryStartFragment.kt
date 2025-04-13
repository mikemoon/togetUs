package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryStartBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryStartFragment : BaseFragment<FragmentDeliveryStartBinding, DeliveryStartViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_start
    override val viewModel: DeliveryStartViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()
    }
}