package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginPasswordSetBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class LoginPasswordSetFragment : BaseFragment<FragmentLoginPasswordSetBinding, LoginPasswordSetViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_login_password_set
    override val viewModel: LoginPasswordSetViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                LoginPasswordSetViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                LoginPasswordSetViewModel.Event.InputComplete -> {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

}