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

                }
                TermViewModel.Event.PrivacyTerm ->{

                }
                TermViewModel.Event.LocationTerm ->{

                }
            }
        }
    }
}