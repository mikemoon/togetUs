package sky.kr.co.newtogetusa.ui.login

import android.text.InputFilter
import androidx.core.content.ContextCompat
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
import sky.kr.co.newtogetusa.databinding.FragmentLoginNicknameBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.AllowedNicknameFilter
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber

@AndroidEntryPoint
class LoginNicknameFragment : BaseFragment<FragmentLoginNicknameBinding, LoginNicknameViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_nickname
    override val viewModel: LoginNicknameViewModel by viewModels()
    private val args : LoginNicknameFragmentArgs by navArgs()

    override fun init() {
        super.init()
        viewModel.userId.value = args.userId
        viewModel.verifyCode.value = args.verifyCode
        /*dataBinding.etNickname.filters = arrayOf(
            AllowedNicknameFilter(),
            InputFilter.LengthFilter(10)
        )*/
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.nickname.observe(viewLifecycleOwner){ textStr ->
            val s = textStr ?: ""
            val validLength = s.length >= 2
            val validChars = s.all { it.isLetterOrDigit() || (it in '가'..'힣') }
            val isValid = validLength && viewModel.isNicknameValid(s)

            dataBinding.tvDesc.apply {
                when {
                    !validLength -> {
                        text = "닉네임을 2자 이상 입력해주세요."
                        setTextColor(ContextCompat.getColor(context, R.color.red_100))
                    }
                    !validChars -> {
                        text = "닉네임은 한글, 영문, 숫자만 가능해요. (10자 이하)"
                        setTextColor(ContextCompat.getColor(context, R.color.black_40))
                    }
                    else -> {
                        text = ""
                        setTextColor(ContextCompat.getColor(context, R.color.black_40))
                    }
                }
            }
            dataBinding.tvConfirm.isEnabled = isValid
        }

        viewModel.checkingNickname.observe(viewLifecycleOwner){ checking ->
            checking?.let {
                if(it){
                    requireContext().toast("중복된 닉네임입니다.")
                }else{
                    viewModel.join(args.termsList.toList()){ result ->
                        if(result){
                            findNavController().navigate(
                                LoginNicknameFragmentDirections.actionLoginNicknameFragmentToLoginStartFragment(
                                    nickname = viewModel.nickname.value.orEmpty()
                                )
                            )
                        }else{
                            requireContext().toast("회원가입에 실패하였습니다.")
                        }
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                is LoginNicknameViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is LoginNicknameViewModel.Event.Confirm -> {
                    viewModel.checkNickname(viewModel.nickname.value.toString())
                }
            }
        }
    }

}
