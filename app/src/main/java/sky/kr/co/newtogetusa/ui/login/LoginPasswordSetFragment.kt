package sky.kr.co.newtogetusa.ui.login

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginPasswordSetBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber

@AndroidEntryPoint
class LoginPasswordSetFragment : BaseFragment<FragmentLoginPasswordSetBinding, LoginPasswordSetViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_login_password_set
    override val viewModel: LoginPasswordSetViewModel by viewModels()
    private val args: LoginPasswordSetFragmentArgs by navArgs()

    override fun initObserver() {
        super.initObserver()

        viewModel.passwordChangeResult.observe(viewLifecycleOwner){ result ->
            Timber.d("changePw ob $result")
            result?.let {
                val termsList = args.termsList
                if (termsList.isNullOrEmpty()) {
                    findNavController().popBackStack(R.id.loginEmailFragment, false)
                } else {
                    findNavController().navigate(
                        LoginPasswordSetFragmentDirections.actionLoginPasswordSetFragmentToLoginNicknameFragment(
                            userId = result.userId,
                            verifyCode = result.verifyCode,
                            termsList = termsList
                        )
                    )
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                LoginPasswordSetViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                LoginPasswordSetViewModel.Event.InputComplete -> {
                    viewModel.changePassword(args.email, viewModel.passwordText.value.toString(), args.verifyCode)
                }
            }
        }
    }

}
