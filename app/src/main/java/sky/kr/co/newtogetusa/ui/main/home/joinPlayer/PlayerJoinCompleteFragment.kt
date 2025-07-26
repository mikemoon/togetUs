package sky.kr.co.newtogetusa.ui.main.home.joinPlayer

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

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                PlayerJoinCompleteViewModel.Event.MoveToHome -> {
                    findNavController().navigate(R.id.action_playerJoinCompleteFragment_to_homeTabFragment)
                }
            }
        }
    }
}