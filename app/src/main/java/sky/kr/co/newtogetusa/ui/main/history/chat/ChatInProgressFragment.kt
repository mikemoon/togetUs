package sky.kr.co.newtogetusa.ui.main.history.chat

import android.net.Uri
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
    private val chatAdapter = ChatInProgressAdapter(
        onClick = { viewModel.onRoomClick(it.roomId) },
        onSelect = { viewModel.selectPlayer(args.deliveryId, it.playerId) },
        onReject = { viewModel.rejectApply(args.deliveryId, it.playerId) },
        onCancelSuggest = { viewModel.cancelSuggest(args.deliveryId, it.playerId) }
    )

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        chatAdapter.isSelectMode = args.isSelectMode
        dataBinding.rvRooms.adapter = chatAdapter
        viewModel.load(args.deliveryId)
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rooms.collect { rooms ->
                    chatAdapter.submitItems(rooms)
                    dataBinding.llEmpty.isVisible = rooms.isEmpty()
                }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                ChatInProgressViewModel.Event.Back -> findNavController().popBackStack()
                ChatInProgressViewModel.Event.LoadFailed -> requireContext().toast("채팅 목록을 불러오지 못했습니다.")
                ChatInProgressViewModel.Event.SelectFailed -> requireContext().toast("플레이어 선택에 실패했습니다.")
                ChatInProgressViewModel.Event.ActionFailed -> requireContext().toast("요청에 실패했습니다.")
                ChatInProgressViewModel.Event.RejectSuccess -> requireContext().toast("동행 지원을 거절하였어요.")
                ChatInProgressViewModel.Event.CancelSuggestSuccess -> requireContext().toast("제안이 취소되었어요.")
                is ChatInProgressViewModel.Event.OpenRoom -> findNavController().navigate(
                    Uri.parse("togetus://chat-room/${event.roomId}")
                )
                is ChatInProgressViewModel.Event.OpenPayment -> findNavController().navigate(
                    R.id.deliveryPayFragment,
                    bundleOf(
                        "deliveryId" to event.deliveryId,
                        "playerId" to event.playerId
                    )
                )
            }
        }
    }
}
