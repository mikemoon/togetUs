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
import timber.log.Timber

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
        viewModel.getTerms()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(this) { event ->
            when(event) {
                is LoginTermAgreeViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginTermAgreeViewModel.Event.Agree -> {
                    val selectedCodes: Array<String> =
                        viewModel.termsList.value
                            ?.map { it.code }                 // List<String>
                            ?.filter { it != "marketing" }    // 기본: 마케팅 제외
                            ?.let { base ->
                                if (viewModel.agreeMarketing.value) base + "marketing" else base
                            }
                            ?.distinct()                      // 혹시 중복 방지
                            ?.toTypedArray()
                            ?: emptyArray()
                    if (args.isEmailJoin) {
                        findNavController().navigate(
                            LoginTermAgreeFragmentDirections.actionLoginTermAgreeFragmentToLoginJoinEmailFragment(
                                isFindPassword = false,
                                termsList = selectedCodes
                            )
                        )
                    } else {
                        findNavController().navigate(LoginTermAgreeFragmentDirections.actionLoginTermAgreeFragmentToLoginNicknameFragment(
                            userId = viewModel.userId.value,
                            verifyCode = viewModel.verifyCode.value,
                            termsList = selectedCodes
                        ))
                    }
                }

            }
        }

    }


}
