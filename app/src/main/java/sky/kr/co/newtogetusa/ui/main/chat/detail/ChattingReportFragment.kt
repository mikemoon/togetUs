package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingReportBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.custom.CustomItemDecoration

@AndroidEntryPoint
class ChattingReportFragment : BaseFragment<FragmentChattingReportBinding, ChattingReportViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_report
    override val viewModel: ChattingReportViewModel by viewModels()
    private val args: ChattingReportFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.rvReason.apply {
            adapter = ChattingReportAdapter(viewModel).apply {
                items = listOf(ReportReasonModel(1,"거래 중 분쟁"),
                    ReportReasonModel(2,"음란/성적 행위"),ReportReasonModel(3,"욕설/비방/혐오"),
                    ReportReasonModel(4, "사기/사칭"), ReportReasonModel(5, "스팸"), ReportReasonModel(6,"기타")).toMutableList()
            }
            addItemDecoration(CustomItemDecoration(context, ContextCompat.getDrawable(context, R.drawable.list_divider)))
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                ChattingReportViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ChattingReportViewModel.Event.Reason -> {
                    findNavController().navigate(
                        ChattingReportFragmentDirections.actionChattingReportFragmentToChattingReportDetailFragment(
                            args.roomId,
                            event.reasonType,
                            event.reason,
                        )
                    )
                }
            }
        }
    }
}
