package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentFaqDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class FAQDetailFragment : BaseFragment<FragmentFaqDetailBinding, FAQDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_faq_detail
    override val viewModel: FAQDetailViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is FAQDetailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}