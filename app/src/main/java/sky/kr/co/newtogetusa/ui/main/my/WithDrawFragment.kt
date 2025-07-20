package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentWithdrawBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class WithDrawFragment : BaseFragment<FragmentWithdrawBinding, WithDrawViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_withdraw
    override val viewModel: WithDrawViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                WithDrawViewModel.Event.Back -> {
                    if (viewModel.withDrawStep.value == 1) {
                        findNavController().popBackStack()
                    } else {
                        viewModel.withDrawStep.value = 1
                    }
                }

                WithDrawViewModel.Event.WithDraw -> {
                    viewModel.withDrawStep.value = 2
                }
            }
        }
    }
}