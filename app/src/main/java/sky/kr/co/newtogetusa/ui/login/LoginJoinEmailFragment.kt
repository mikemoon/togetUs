package sky.kr.co.newtogetusa.ui.login

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginJoinEmailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class LoginJoinEmailFragment : BaseFragment<FragmentLoginJoinEmailBinding, LoginJoinEmailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_join_email
    override val viewModel: LoginJoinEmailViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.verifyCodeText.observe(viewLifecycleOwner){ verifyCodeText ->
            if(verifyCodeText.length == 6){
                findNavController().navigate(LoginJoinEmailFragmentDirections.actionLoginJoinEmailFragmentToLoginPasswordSetFragment())
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is LoginJoinEmailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginJoinEmailViewModel.Event.RequestVerifyCode -> {
                    viewModel.verifyLayoutMode.value = true
                    viewModel.startTimeout()
                }
                is LoginJoinEmailViewModel.Event.ReSend -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "인증번호를 다시 발송할까요?",
                            msg = "요청 가능 횟수가 4회 남았어요.",
                            rightBtn = "재요청",
                            leftBtn = "아니오"
                        ).onRightBtn {
                            viewModel.startTimeout()
                        }
                    )
                }

            }
        }

    }

}