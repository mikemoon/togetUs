package sky.kr.co.newtogetusa.ui.login

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
import sky.kr.co.newtogetusa.databinding.FragmentLoginJoinEmailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class LoginJoinEmailFragment :
    BaseFragment<FragmentLoginJoinEmailBinding, LoginJoinEmailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_join_email
    override val viewModel: LoginJoinEmailViewModel by viewModels()
    private val args: LoginJoinEmailFragmentArgs by navArgs()

    override fun init() {
        super.init()

        viewModel.setIsPasswordMode(args.isFindPassword)
    }

    override fun initObserver() {
        super.initObserver()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collectLatest { isLoading ->
                    if (isLoading) showLoading() else hideLoading()
                }
            }
        }

        viewModel.verifyCodeText.observe(viewLifecycleOwner) { verifyCodeText ->
            if (verifyCodeText.length == 6) {
                viewModel.certifyVerifyCode(verifyCodeText)
            }
        }

        viewModel.certCodeResult.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    LoginJoinEmailFragmentDirections.actionLoginJoinEmailFragmentToLoginPasswordSetFragment(
                        email = viewModel.emailText.value.toString(),
                        verifyCode = viewModel.verifyCodeText.value.toString(),
                        termsList = args.termsList
                    )
                )
            }
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                is LoginJoinEmailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                is LoginJoinEmailViewModel.Event.RequestVerifyCode -> {
                    viewModel.requestVerifyEmail(viewModel.emailText.value.toString())
                }

                is LoginJoinEmailViewModel.Event.VerifyEmailRequested -> {
                    viewModel.verifyLayoutMode.value = true
                    viewModel.titleText.value = "이메일로 받은 인증번호를 입력해 주세요"
                    viewModel.startTimeout()
                }

                is LoginJoinEmailViewModel.Event.ReSend -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "인증번호를 다시 발송할까요?",
                            msg = viewModel.resendRemainingMessage(),
                            rightBtn = "재요청",
                            leftBtn = "아니오"
                        ).onRightBtn {
                            viewModel.requestVerifyEmail(
                                viewModel.emailText.value.toString(),
                                isResend = true
                            )
                        }
                    )
                }

                is LoginJoinEmailViewModel.Event.ShowMessage -> {
                    requireContext().toast(it.message)
                }

            }
        }

    }

}
