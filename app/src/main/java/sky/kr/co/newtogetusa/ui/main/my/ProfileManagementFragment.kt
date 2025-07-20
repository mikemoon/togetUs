package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentProfileManagementBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ProfileManagementFragment : BaseFragment<FragmentProfileManagementBinding, ProfileManagementViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_management
    override val viewModel: ProfileManagementViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ProfileManagementViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ProfileManagementViewModel.Event.ModifyProfile -> {
                    findNavController().navigate(R.id.action_profileManagementFragment_to_modifyProfileFragment)
                }
                is ProfileManagementViewModel.Event.PasswordSet -> {
                    findNavController().navigate(R.id.action_profileManagementFragment_to_passwordSetFragment)
                }
            }
        }
    }
}