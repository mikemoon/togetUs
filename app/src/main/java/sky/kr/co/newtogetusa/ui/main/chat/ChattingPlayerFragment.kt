package sky.kr.co.newtogetusa.ui.main.chat

import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem

@AndroidEntryPoint
class ChattingPlayerFragment : BaseFragment<FragmentChattingPlayerBinding, ChattingPlayerViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_player
    override val viewModel: ChattingPlayerViewModel
        get() = viewModel
    override fun init() {
        super.init()

        dataBinding.rvList.apply {
            adapter = ChattingPlayerAdapter().apply {
                items = mutableListOf(ChatListItem("홍길동", "• 오후 12:25", "", 1),ChatListItem("김좌진", "• 오후 10:25", "", 100),ChatListItem("세종대왕", "• 오전 10:25", "", 12))
            }
        }
    }
}