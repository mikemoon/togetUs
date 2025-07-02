package sky.kr.co.newtogetusa.ui.login

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginNicknameBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class LoginNicknameFragment : BaseFragment<FragmentLoginNicknameBinding, LoginNicknameViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_nickname
    override val viewModel: LoginNicknameViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.nickname.observe(viewLifecycleOwner){
            if(it.length == 1){
                dataBinding.tvDesc.apply {
                    text = "닉네임을 2자 이상 입력해주세요."
                    setTextColor(ContextCompat.getColor(context, R.color.red_100))
                }
            }else{
                dataBinding.tvDesc.apply {
                    text = "닉네임은 한글, 영문, 숫자만 가능해요. (10자 이하)"
                    setTextColor(ContextCompat.getColor(context, R.color.black_40))
                }
            }
            dataBinding.tvConfirm.isEnabled = it.length >= 2
        }

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                is LoginNicknameViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginNicknameViewModel.Event.Confirm -> {
                    findNavController().navigate(LoginNicknameFragmentDirections.actionLoginNicknameFragmentToLoginStartFragment())
                }
            }
        }
    }

}