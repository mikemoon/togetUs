package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginEmailBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.hideKeyboard

@AndroidEntryPoint
class LoginEmailFragment : BaseFragment<FragmentLoginEmailBinding, LoginEmailViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_login_email
    override val viewModel: LoginEmailViewModel by viewModels()

    override fun init() {
        super.init()

        // 이메일 입력 후 키보드 '다음' 버튼 → 키보드 닫기
        dataBinding.etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                dataBinding.etEmail.clearFocus()
                requireContext().hideKeyboard(dataBinding.etEmail)
                true
            } else {
                false
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.failMessage.observe(viewLifecycleOwner){
                    MessageDialog.newInstance(
                        msgTitle = "로그인 실패",
                        msg = it,
                        rightBtn = "확인"
                    ).show(childFragmentManager, "")
                }
            }
        }

        viewModel.loginResult.observe(viewLifecycleOwner){ result ->
            if(!result.accessToken.isNullOrEmpty()){
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is LoginEmailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginEmailViewModel.Event.JoinByEmail -> {
                    findNavController().navigate(
                        LoginEmailFragmentDirections.actionLoginEmailFragmentToLoginTermAgreeFragment(
                            isEmailJoin = true
                        )
                    )
                }
                is LoginEmailViewModel.Event.Login -> {
                    viewModel.login(viewModel.email.value.orEmpty(), viewModel.password.value.orEmpty())
                }
                is LoginEmailViewModel.Event.FindPassword -> {
                    findNavController().navigate(LoginEmailFragmentDirections.actionLoginEmailFragmentToLoginJoinEmailFragment(true))
                }
                else ->{

                }
            }
        }
    }

}
