package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginEmailBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class LoginEmailFragment : BaseFragment<FragmentLoginEmailBinding, LoginEmailViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_login_email
    override val viewModel: LoginEmailViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is LoginEmailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginEmailViewModel.Event.JoinByEmail -> {
                    findNavController().navigate(LoginEmailFragmentDirections.actionLoginEmailFragmentToLoginJoinEmailFragment())
                }
                is LoginEmailViewModel.Event.Login -> {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
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