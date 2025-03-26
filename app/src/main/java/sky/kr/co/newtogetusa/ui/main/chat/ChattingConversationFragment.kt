package sky.kr.co.newtogetusa.ui.main.chat

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingConversationBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingConversationFragment : BaseFragment<FragmentChattingConversationBinding, ChattingConversationViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_conversation
    override val viewModel: ChattingConversationViewModel by viewModels()

    private lateinit var adapter: ChatMessageAdapter

    override fun init() {
        super.init()
        viewModel.connect()
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter()
        dataBinding.recyclerViewMessages.apply {
            adapter = this@ChattingConversationFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // 아래에서부터 쌓기
            }
        }
    }
}