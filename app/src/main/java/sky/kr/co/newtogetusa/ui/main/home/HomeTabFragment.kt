package sky.kr.co.newtogetusa.ui.main.home

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHomeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeBinding, HomeTabViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_home
    override val viewModel: HomeTabViewModel by viewModels()

    override fun init() {
        super.init()


    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.tvDelivery.setOnClickListener {
            findNavController().navigate(R.id.action_homeTabFragment_to_deliveryReqFragment)
        }
    }
}