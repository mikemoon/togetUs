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
import sky.kr.co.newtogetusa.databinding.FragmentLoginTermAgreeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class LoginTermAgreeFragment :
    BaseFragment<FragmentLoginTermAgreeBinding, LoginTermAgreeViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_term_agree
    override val viewModel: LoginTermAgreeViewModel by viewModels()
    private val args : LoginTermAgreeFragmentArgs by navArgs()

    override fun init() {
        super.init()

        viewModel.userId.value = args.userId
        viewModel.verifyCode.value = args.verifyCode
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(this) { event ->
            when(event) {
                is LoginTermAgreeViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginTermAgreeViewModel.Event.Agree -> {
                    findNavController().navigate(LoginTermAgreeFragmentDirections.actionLoginTermAgreeFragmentToLoginNicknameFragment(
                        userId = viewModel.userId.value,
                        verifyCode = viewModel.verifyCode.value,
                        termsList = arrayOf(
                            "use", "personal", "3-party", "location"
                        )
                    ))
                }

            }
        }

    }


}