package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryRequestSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryRequestSearchFragment : BaseFragment<FragmentDeliveryRequestSearchBinding, DeliveryRequestSearchViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_request_search
    override val viewModel: DeliveryRequestSearchViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()
    }
}