package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentProfileMangeSubBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ProfileManageSubFragment : BaseFragment<FragmentProfileMangeSubBinding, ProfileManageSubVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_mange_sub
    override val viewModel: ProfileManageSubVM by viewModels()

    private val args : ProfileManageSubFragmentArgs by navArgs()

    override fun init() {
        super.init()

        dataBinding.profile = args.profileDto
        dataBinding.tvPhone.text = "-"
        dataBinding.tvDate.text = "※ 최근 인증: -"
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginAccountText.collectLatest {
                        dataBinding.tvEmail.text = it
                    }
                }
                launch {
                    viewModel.loginTypeLabel.collectLatest { type ->
                        dataBinding.tvSnsTitle.text = if (type == "이메일") "이메일 로그인" else "SNS 로그인"
                        val iconRes = when (type) {
                            "카카오" -> R.drawable.login_kakao
                            "네이버" -> R.drawable.login_naver
                            "구글" -> R.drawable.login_google
                            "이메일" -> R.drawable.login_mail
                            else -> R.drawable.login_mail
                        }
                        dataBinding.ivSns.setImageResource(iconRes)
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ProfileManageSubVM.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ProfileManageSubVM.Event.ModifyProfile -> {
                    findNavController().navigate(ProfileManageSubFragmentDirections.actionProfileManageSubFragmentToModifyProfileFragment(args.profileDto))
                }
                is ProfileManageSubVM.Event.PasswordSet -> {
                    findNavController().navigate(R.id.action_profileManageSubFragment_to_passwordSetFragment)
                }
                is ProfileManageSubVM.Event.Recertification -> {

                }
            }
        }
    }
}
