package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingReportDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ChattingReportDetailFragment : BaseFragment<FragmentChattingReportDetailBinding, ChattingReportDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_report_detail
    override val viewModel: ChattingReportDetailViewModel by viewModels()
    private val args: ChattingReportDetailFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.tvRequest.isSelected = false
        dataBinding.viewModel = viewModel
        dataBinding.tvTitle.text = args.reason
    }

    override fun initObserver() {
        super.initObserver()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.editTextChangedFlow.collectLatest { changedText ->
                        dataBinding.tvRequest.isSelected = changedText.isNotEmpty()
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                ChattingReportDetailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                ChattingReportDetailViewModel.Event.Send ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "신고를 접수하겠어요?",
                            msg = "${args.reason} 사유로 신고 접수가 이루어져요.",
                            leftBtn = "취소",
                            rightBtn = "접수하기"
                        ).onRightBtn {
                            viewModel.report(args.roomId)
                        }
                    )
                }
                ChattingReportDetailViewModel.Event.ReportSuccess -> {
                    requireContext().toast("신고가 접수되었습니다.")
                    findNavController().popBackStack(R.id.chattingConversationFragment, false)
                }
                ChattingReportDetailViewModel.Event.ReportFailed -> {
                    requireContext().toast("신고 접수에 실패했습니다.")
                }
            }
        }
    }
}
