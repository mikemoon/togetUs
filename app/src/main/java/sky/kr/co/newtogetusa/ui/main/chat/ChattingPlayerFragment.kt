package sky.kr.co.newtogetusa.ui.main.chat

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchRoomDto
import sky.kr.co.newtogetusa.data.remote.request.chat.ChatRoomSearchRequest
import sky.kr.co.newtogetusa.databinding.FragmentChattingPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem
import timber.log.Timber

@AndroidEntryPoint
class ChattingPlayerFragment : BaseFragment<FragmentChattingPlayerBinding, ChattingPlayerViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_player
    override val viewModel: ChattingPlayerViewModel by viewModels()
    private val tabViewModel: ChattingTabViewModel by viewModels({ requireParentFragment() })
    private var selectedFilter = ChatFilter.ALL
    private lateinit var chatAdapter: ChattingPlayerAdapter

    override fun init() {
        super.init()

        setupFilterTabs()
        chatAdapter = ChattingPlayerAdapter { item ->
            viewModel.onChatItemClick(item)
        }
        dataBinding.rvList.apply {
            adapter = chatAdapter
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                is ChattingPlayerViewModel.Event.ChattingSelect ->{
                    Timber.d("ChattingSel")
                    findNavController().navigate(
                        ChattingTabFragmentDirections.actionChattingTabFragmentToChattingConversationFragment(
                            event.item.roomId.toLong()
                        )
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tabViewModel.playerRoomPagingData
                        .map { pagingData -> pagingData.map { room -> room.toChatListItem() } }
                        .collectLatest { pagingData ->
                            chatAdapter.submitData(pagingData)
                        }
                }

                launch {
                    chatAdapter.loadStateFlow.collectLatest { loadStates ->
                        val isEmpty = loadStates.refresh is LoadState.NotLoading && chatAdapter.itemCount == 0
                        dataBinding.llEmpty.isVisible = isEmpty
                        dataBinding.rvList.isVisible = !isEmpty
                    }
                }
            }
        }
    }

    private fun setupFilterTabs() {
        dataBinding.tvFilterAll.setOnClickListener {
            updateFilterTab(ChatFilter.ALL)
        }
        dataBinding.tvFilterProgress.setOnClickListener {
            updateFilterTab(ChatFilter.PROGRESS)
        }
        dataBinding.tvFilterEnd.setOnClickListener {
            updateFilterTab(ChatFilter.END)
        }
        updateFilterTab(selectedFilter)
    }

    private fun updateFilterTab(filter: ChatFilter) {
        selectedFilter = filter
        bindFilterTab(dataBinding.tvFilterAll, filter == ChatFilter.ALL)
        bindFilterTab(dataBinding.tvFilterProgress, filter == ChatFilter.PROGRESS)
        bindFilterTab(dataBinding.tvFilterEnd, filter == ChatFilter.END)
        tabViewModel.searchPlayerRooms(
            ChatRoomSearchRequest(
                type = filter.apiType,
                page_no = 0,
                page_size = 10
            )
        )
    }

    private fun bindFilterTab(view: TextView, selected: Boolean) {
        view.setBackgroundResource(
            if (selected) R.drawable.background_s_b80_r24
            else R.drawable.background_st_b10_s_w_r24
        )
        view.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selected) R.color.white else R.color.black_40
            )
        )
        view.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
    }

    private enum class ChatFilter {
        ALL, PROGRESS, END;

        val apiType: String
            get() = when (this) {
                ALL -> ChattingTabViewModel.TYPE_ALL
                PROGRESS -> ChattingTabViewModel.TYPE_PROGRESS
                END -> ChattingTabViewModel.TYPE_END
            }
    }

    private fun ChatRoomSearchRoomDto.toChatListItem(): ChatListItem {
        val participant = user ?: player
        return ChatListItem(
            roomId = room_id,
            name = participant?.nickname ?: room_name,
            date = last_msg?.display_send_date?.let { "• $it" }.orEmpty(),
            profileUrl = participant?.profile_image.orEmpty(),
            unReadCount = unread_cnt,
            message = last_msg?.msg.orEmpty()
        )
    }
}
