package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentProfileMangeSubBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ProfileManageSubFragment : BaseFragment<FragmentProfileMangeSubBinding, ProfileManageSubVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_mange_sub
    override val viewModel: ProfileManageSubVM by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()
    }
}