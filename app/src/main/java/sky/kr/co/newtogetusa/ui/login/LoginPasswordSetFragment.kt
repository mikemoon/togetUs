package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginPasswordSetBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber

@AndroidEntryPoint
class LoginPasswordSetFragment : BaseFragment<FragmentLoginPasswordSetBinding, LoginPasswordSetViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_login_password_set
    override val viewModel: LoginPasswordSetViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.passwordChangeResult.observe(viewLifecycleOwner){ result ->
            Timber.d("changePw ob $result")
            result?.let {
                val userId = result.userId
                val verifyCode = result.verifyCode
                viewModel.join(userId, verifyCode)
            }
        }

        viewModel.joinResult.observe(viewLifecycleOwner){ joinResult ->
            joinResult?.let {
                moveToMain()
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                LoginPasswordSetViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                LoginPasswordSetViewModel.Event.InputComplete -> {
                    val email = LoginPasswordSetFragmentArgs.fromBundle(requireArguments()).email
                    val verifyCode = LoginPasswordSetFragmentArgs.fromBundle(requireArguments()).verifyCode
                    viewModel.changePassword(email, viewModel.passwordText.value.toString(), verifyCode)
                }
            }
        }
    }

    private fun moveToMain(){
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

}