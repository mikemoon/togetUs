package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

        noticeAdapter = NoticeAdapter(viewModel)
        dataBinding.rv.apply {
            adapter = noticeAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        viewModel.getNoticeList()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.noticeList.collect{
                    noticeAdapter.setItems(it)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is NoticeViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is NoticeViewModel.Event.NoticeDetail -> {
                    val action = NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(it.id)
                    findNavController().navigate(action)
                }
            }
        }
    }

}