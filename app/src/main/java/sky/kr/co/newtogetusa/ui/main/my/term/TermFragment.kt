package sky.kr.co.newtogetusa.ui.main.my.term

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentTermBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class TermFragment : BaseFragment<FragmentTermBinding, TermViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_term
    override val viewModel: TermViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                TermViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                TermViewModel.Event.UseTerm ->{
                    navigateTermDetail(TermViewModel.TermType.USE)
                }
                TermViewModel.Event.PrivacyTerm ->{
                    navigateTermDetail(TermViewModel.TermType.PRIVACY)
                }
                TermViewModel.Event.LocationTerm ->{
                    navigateTermDetail(TermViewModel.TermType.LOCATION)
                }
            }
        }
    }

    private fun navigateTermDetail(type: TermViewModel.TermType) {
        val term = viewModel.findTerm(type)
        findNavController().navigate(
            TermFragmentDirections.actionTermFragmentToTermDetailFragment(
                title = term?.name ?: type.title,
                content = term?.description?.takeIf { it.isNotBlank() } ?: type.fallbackContent
            )
        )
    }
}
