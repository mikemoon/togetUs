package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.rvReason.apply {
            adapter = ChattingReportAdapter().apply {
                items = listOf("거래 중 분쟁","욕설/비방/혐오", "음란/성적 행위", "사기/사칭", "스팸", "기타").toMutableList()
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
            }
        }
    }
}