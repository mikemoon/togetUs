package sky.kr.co.newtogetusa.ui.main.chat

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import sky.kr.co.newtogetusa.databinding.FragmentChattingUserBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.chat.data.ChatListItem
import javax.inject.Inject

@AndroidEntryPoint
class ChattingUserFragment @Inject constructor(
) : BaseFragment<FragmentChattingUserBinding, ChattingUserViewModel>() {
    override val layoutId = R.layout.fragment_chatting_user
    override val viewModel: ChattingUserViewModel by viewModels()
    private val tabViewModel: ChattingTabViewModel by viewModels({ requireParentFragment() })
    private var selectedFilter = ChatFilter.ALL
    private lateinit var chatAdapter: ChattingPlayerAdapter

    override fun init() {
        super.init()

        setupFilterTabs()
        chatAdapter = ChattingPlayerAdapter { item ->
            findNavController().navigate(
                R.id.action_global_chattingConversationFragment,
                bundleOf(
                    "roomId" to item.roomId.toLong(),
                    "isPlayerRoom" to false
                )
            )
        }
        dataBinding.rvList.apply {
            adapter = chatAdapter
            itemAnimator = null
        }
    }

    override fun onResume() {
        super.onResume()
        if (::chatAdapter.isInitialized) {
            chatAdapter.refresh()
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tabViewModel.userRoomPagingData
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

                launch {
                    ChatRoomListUpdateBus.updates.collectLatest { update ->
                        chatAdapter.applyRoomUpdate(update)
                        if (update.message == null && update.date == null) {
                            chatAdapter.refresh()
                        }
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
        tabViewModel.searchUserRooms(
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
        val participant = player ?: user
        return ChatListItem(
            roomId = room_id,
            name = participant?.nickname ?: room_name,
            date = last_msg?.display_send_date?.let { "• $it" }.orEmpty(),
            profileUrl = participant?.profile_image.orEmpty(),
            deliveryImageUrl = delivery.prd_picture,
            unReadCount = unread_cnt,
            message = last_msg.toPreviewMessage(),
            isDisabled = isDisabled
        )
    }

    private fun sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomLastMessageDto?.toPreviewMessage(): String {
        val message = this?.msg.orEmpty()
        return when {
            this == null -> ""
            mimetype.startsWith("image/") || message.isAttachUrl() -> "이미지를 보냈습니다."
            mimetype.startsWith("video/") -> "영상을 보냈습니다."
            else -> message
        }
    }

    private fun String.isAttachUrl(): Boolean =
        (startsWith("http://") || startsWith("https://")) && contains("/attach/")
}
