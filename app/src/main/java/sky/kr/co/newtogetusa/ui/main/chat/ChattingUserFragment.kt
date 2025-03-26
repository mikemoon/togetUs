package sky.kr.co.newtogetusa.ui.main.chat

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingUserBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class ChattingUserFragment @Inject constructor(
) : BaseFragment<FragmentChattingUserBinding, ChattingUserViewModel>() {
    override val layoutId = R.layout.fragment_chatting_user
    override val viewModel: ChattingUserViewModel by viewModels()
}