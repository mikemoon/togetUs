package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentProfileManagementBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ProfileManagementFragment : BaseFragment<FragmentProfileManagementBinding, ProfileManagementViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_management
    override val viewModel: ProfileManagementViewModel by viewModels()
    private val args : ProfileManagementFragmentArgs by navArgs()

    var profileDto : ProfileDto? = null

    override fun init() {
        super.init()
        profileDto = args.profileDto
        dataBinding.profile = profileDto

        viewModel.getMyProfile {
            dataBinding.profile = it
            profileDto = it
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ProfileManagementViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ProfileManagementViewModel.Event.ModifyProfile -> {
                    findNavController().navigate(ProfileManagementFragmentDirections.actionProfileManagementFragmentToModifyProfileFragment(profileDto))
                }
                is ProfileManagementViewModel.Event.PasswordSet -> {
                    findNavController().navigate(R.id.action_profileManagementFragment_to_passwordSetFragment)
                }
            }
        }
    }
}