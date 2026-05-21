package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAccompanyCreditBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class AccompanyCreditFragment : BaseFragment<FragmentAccompanyCreditBinding, AccompanyCreditViewModel>() {

    override val layoutId: Int = R.layout.fragment_accompany_credit
    override val viewModel: AccompanyCreditViewModel by viewModels()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                AccompanyCreditViewModel.Event.Back -> findNavController().popBackStack()
            }
        }
    }
}
