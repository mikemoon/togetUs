package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentFaqDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class FAQDetailFragment : BaseFragment<FragmentFaqDetailBinding, FAQDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_faq_detail
    override val viewModel: FAQDetailViewModel by viewModels()


    override fun init() {
        super.init()
        arguments?.getInt("faqId")?.let {
            viewModel.getFaqDetail(it)
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.faqDetail.collectLatest {
                    dataBinding.tvTitle.text = it?.title
                    dataBinding.tvContent.text = it?.contents
                    dataBinding.tvDate.text = it?.regDate
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is FAQDetailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}