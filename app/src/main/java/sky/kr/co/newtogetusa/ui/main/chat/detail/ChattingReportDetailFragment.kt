package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingReportDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingReportDetailFragment : BaseFragment<FragmentChattingReportDetailBinding, ChattingReportDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_report_detail
    override val viewModel: ChattingReportDetailViewModel by viewModels()

    override fun init() {
        super.init()
        dataBinding.tvRequest.isSelected = false
        dataBinding.viewModel = viewModel
        dataBinding.tvTitle.text = arguments?.getString("title")
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
            }
        }
    }
}