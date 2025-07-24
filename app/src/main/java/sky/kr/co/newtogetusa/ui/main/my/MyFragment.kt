package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentMyBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding, MyViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_my
    override val viewModel: MyViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                MyViewModel.Event.MySetting ->{
                    findNavController().navigate(R.id.action_myFragment_to_mySettingFragment)
                }
                MyViewModel.Event.ProfileManage ->{
                    findNavController().navigate(R.id.action_myFragment_to_profileManagementFragment)
                }
                MyViewModel.Event.Notice ->{
                    findNavController().navigate(R.id.action_myFragment_to_noticeFragment)
                }
                MyViewModel.Event.FAQ ->{
                    findNavController().navigate(R.id.action_myFragment_to_FAQFragment)
                }
                MyViewModel.Event.Settle ->{
                    findNavController().navigate(R.id.action_myFragment_to_settleFragment)
                }
            }
        }
    }

}