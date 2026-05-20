package sky.kr.co.newtogetusa.ui.main.history.chat

import androidx.core.view.isVisible
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChatInProgressBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ChatInProgressFragment : BaseFragment<FragmentChatInProgressBinding, ChatInProgressViewModel>() {
    override val layoutId: Int = R.layout.fragment_chat_in_progress
    override val viewModel: ChatInProgressViewModel by viewModels()
    private val args: ChatInProgressFragmentArgs by navArgs()
    private val chatAdapter = ChatInProgressAdapter { viewModel.onRoomClick(it.roomId) }

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.rvRooms.adapter = chatAdapter
        viewModel.load(args.deliveryId)
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rooms.collect { rooms ->
                    chatAdapter.submitItems(rooms)
                    dataBinding.tvEmpty.isVisible = rooms.isEmpty()
                }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                ChatInProgressViewModel.Event.Back -> findNavController().popBackStack()
                ChatInProgressViewModel.Event.LoadFailed -> requireContext().toast("채팅 목록을 불러오지 못했습니다.")
                is ChatInProgressViewModel.Event.OpenRoom -> findNavController().navigate(
                    R.id.chattingConversationFragment,
                    bundleOf("roomId" to event.roomId)
                )
            }
        }
    }
}
