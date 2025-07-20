package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentNoticeDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class NoticeDetailFragment : BaseFragment<FragmentNoticeDetailBinding, NoticeDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_notice_detail
    override val viewModel: NoticeDetailViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                NoticeDetailViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
            }
        }
    }
}