package sky.kr.co.newtogetusa.ui.main.chat

import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingPlayerFragment : BaseFragment<FragmentChattingPlayerBinding, ChattingPlayerViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_player
    override val viewModel: ChattingPlayerViewModel
        get() = viewModel
    override fun init() {
        super.init()
    }
}