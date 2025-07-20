package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentFaqBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class FAQFragment :BaseFragment<FragmentFaqBinding, FAQViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_faq
    override val viewModel: FAQViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is FAQViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}