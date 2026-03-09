package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentNoticeDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class NoticeDetailFragment : BaseFragment<FragmentNoticeDetailBinding, NoticeDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_notice_detail
    override val viewModel: NoticeDetailViewModel by viewModels()

    val args: NoticeDetailFragmentArgs by navArgs()

    override fun initObserver() {
        super.initObserver()

        viewModel.getNoticeDetail(args.id)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.noticeDetail.filterNotNull().collectLatest {
                    dataBinding.tvTitle.text = it.title
                    dataBinding.tvDate.text = it.regDate
                    dataBinding.tvContent.text = it.contents
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                NoticeDetailViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
            }
        }
    }
}