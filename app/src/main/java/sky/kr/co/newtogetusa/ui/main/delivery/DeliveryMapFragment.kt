package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryMapBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class DeliveryMapFragment : BaseFragment<FragmentDeliveryMapBinding, DeliveryMapViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_map
    override val viewModel: DeliveryMapViewModel by viewModels()
}