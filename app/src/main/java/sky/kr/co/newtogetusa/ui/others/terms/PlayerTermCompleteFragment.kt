package sky.kr.co.newtogetusa.ui.others.terms

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentPlayerTermCompleteBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber
import kotlin.math.truncate

@AndroidEntryPoint
class PlayerTermCompleteFragment: BaseFragment<FragmentPlayerTermCompleteBinding, PlayerTermCompleteViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_player_term_complete
    override val viewModel: PlayerTermCompleteViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.moveToHome.observe(viewLifecycleOwner){
            Timber.d("popback")
            findNavController().popBackStack(R.id.searchFragment, true)
        }
    }
}