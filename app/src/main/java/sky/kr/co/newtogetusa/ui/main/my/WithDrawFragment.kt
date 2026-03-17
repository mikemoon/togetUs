package sky.kr.co.newtogetusa.ui.main.my

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentWithdrawBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.login.LoginViewModel

@AndroidEntryPoint
class WithDrawFragment : BaseFragment<FragmentWithdrawBinding, WithDrawViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_withdraw
    override val viewModel: WithDrawViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.loginType.filterNotNull().collectLatest { loginType ->
                    when(loginType) {
                        LoginViewModel.GOOGLE ->{
                            dataBinding.llSns.isVisible = true
                            dataBinding.llSns.isVisible = true
                            dataBinding.llSns.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.background_st_b20_s_w_r4
                            )
                            dataBinding.ivSns.setImageResource(R.drawable.login_google)
                            dataBinding.tvSns.text = "구글로 인증하기"
                        }

                        LoginViewModel.KAKAO ->{
                            dataBinding.llSns.isVisible = true
                        }

                        LoginViewModel.NAVER ->{
                            dataBinding.llSns.isVisible = true
                            dataBinding.llSns.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.background_s_03c75a_r4
                            )
                            dataBinding.ivSns.setImageResource(R.drawable.login_naver)
                            dataBinding.tvSns.apply {
                                text = "네이버로 인증하기"
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                            }
                        }
                        LoginViewModel.EMAIL ->{
                            dataBinding.tvEmailVerify.isVisible = true
                        }
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                WithDrawViewModel.Event.Back -> {
                    if (viewModel.withDrawStep.value == 1) {
                        findNavController().popBackStack()
                    } else {
                        viewModel.withDrawStep.value = 1
                    }
                }

                WithDrawViewModel.Event.WithDraw -> {
                }

                WithDrawViewModel.Event.VerifyEmail ->{
                    viewModel.withDrawStep.value = 2
                }
            }
        }
    }
}