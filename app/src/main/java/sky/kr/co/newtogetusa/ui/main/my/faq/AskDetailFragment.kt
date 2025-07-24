package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAskDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class AskDetailFragment : BaseFragment<FragmentAskDetailBinding, AskDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_ask_detail
    override val viewModel: AskDetailViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                AskDetailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}