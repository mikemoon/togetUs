package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentPasswordSetBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class PasswordSetFragment : BaseFragment<FragmentPasswordSetBinding, PasswordSetViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_password_set
    override val viewModel: PasswordSetViewModel by viewModels()
}