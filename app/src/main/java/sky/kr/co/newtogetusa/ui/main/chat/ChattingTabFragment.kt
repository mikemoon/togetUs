package sky.kr.co.newtogetusa.ui.main.chat

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingTabFragment : BaseFragment<FragmentChattingBinding, ChattingTabViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_chatting
    override val viewModel: ChattingTabViewModel by viewModels()

    private lateinit var adapter: ChatMessageAdapter

    override fun init() {
        super.init()
        viewModel.connect()
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter()
        dataBinding.recyclerViewMessages.apply {
            adapter = this@ChattingTabFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // 아래에서부터 쌓기
            }
        }
    }
}