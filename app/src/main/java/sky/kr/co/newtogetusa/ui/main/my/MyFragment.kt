package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentMyBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding, MyViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_my
    override val viewModel: MyViewModel by viewModels()

    override fun init() {
        super.init()

        viewModel.getMyProfile(){
            dataBinding.profile = it
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModeChanging.drop(1).filter { it }.collectLatest {
                    (requireActivity() as MainActivity).showChangeModeAnimation(!viewModel.isPlayerModeFlow.value)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                MyViewModel.Event.MySetting ->{
                    findNavController().navigate(R.id.action_myFragment_to_mySettingFragment)
                }
                MyViewModel.Event.ProfileManage ->{
                    findNavController().navigate(MyFragmentDirections.actionMyFragmentToProfileManagementFragment(viewModel.profileDto.value))
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
                MyViewModel.Event.Favor ->{
                    findNavController().navigate(R.id.action_myFragment_to_favorPlayerFragment)
                }
                MyViewModel.Event.JoinPlayer ->{
                    findNavController().navigate(R.id.action_myFragment_to_playerJoinFragment2)
                }
                MyViewModel.Event.Term ->{
                    findNavController().navigate(MyFragmentDirections.actionMyFragmentToTermFragment())
                }
                MyViewModel.Event.UseHistory ->{

                }
                MyViewModel.Event.FavorPlayer ->{

                }
            }
        }
    }

}