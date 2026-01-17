package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentProfileIntroduceBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.my.ProfileManagementViewModel

@AndroidEntryPoint
class ProfileIntroduceFragment : BaseFragment<FragmentProfileIntroduceBinding, ProfileManagementViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_profile_introduce
    override val viewModel: ProfileManagementViewModel by viewModels({requireParentFragment()})


}