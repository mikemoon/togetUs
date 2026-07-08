package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginStartBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class LoginStartFragment : BaseFragment<FragmentLoginStartBinding, LoginStartViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_start
    override val viewModel: LoginStartViewModel by viewModels()
    private val args: LoginStartFragmentArgs by navArgs()

    override fun init() {
        super.init()
        viewModel.nickname.value = args.nickname
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is LoginStartViewModel.Event.Start -> {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                else->{

                }
            }
        }
    }
}
