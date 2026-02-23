package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentFavorPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class PlayerJoinCompleteFragment : BaseFragment<FragmentFavorPlayerBinding, PlayerJoinCompleteViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_player_join_complete
    override val viewModel: PlayerJoinCompleteViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                PlayerJoinCompleteViewModel.Event.MoveToHome -> {
                    findNavController().navigate(R.id.action_global_home)
                }
            }
        }
    }
}