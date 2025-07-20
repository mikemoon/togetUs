package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentNoticeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class NoticeFragment : BaseFragment<FragmentNoticeBinding, NoticeViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_notice
    override val viewModel: NoticeViewModel by viewModels()
    private lateinit var noticeAdapter: NoticeAdapter

    override fun init() {
        super.init()

        noticeAdapter = NoticeAdapter(viewModel).apply {
            setItems(listOf("1"))
        }
        dataBinding.rv.apply {
            adapter = noticeAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is NoticeViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is NoticeViewModel.Event.NoticeDetail -> {
                    findNavController().navigate(R.id.action_noticeFragment_to_noticeDetailFragment)
                }
            }
        }
    }

}