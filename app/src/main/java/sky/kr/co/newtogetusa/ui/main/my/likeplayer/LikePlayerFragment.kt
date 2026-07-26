package sky.kr.co.newtogetusa.ui.main.my.likeplayer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentLikePlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class LikePlayerFragment : BaseFragment<FragmentLikePlayerBinding, LikePlayerViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_like_player
    override val viewModel: LikePlayerViewModel by viewModels()

    private lateinit var adapter: LikePlayerAdapter

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel

        adapter = LikePlayerAdapter(
            onItemClick = { player ->
                navigateToProfile(player.player_id, player.nickname, player.profile_image, player.enable)
            },
            onUnlikeClick = { player ->
                viewModel.unlikePlayer(player.player_id)
            }
        )

        dataBinding.rvPlayers.adapter = adapter

        dataBinding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        dataBinding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchLikes()
            dataBinding.swipeRefresh.isRefreshing = false
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.players.collect {
                    adapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is LikePlayerViewModel.Event.ShowMessage -> {
                            requireContext().toast(event.message)
                            viewModel.consumeEvent()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun navigateToProfile(playerId: Long, nickname: String, profileImage: String?, enable: Boolean) {
        val profileDto = ProfileDto(
            user = ProfileDto.User(
                user_id = 0,
                player_id = playerId.toInt(),
                nickname = nickname,
                profile_image = profileImage,
                enable = enable
            ),
            evaluation = ProfileDto.Evaluation(0, 0),
            requst_count = 0,
            review_count = 0
        )
        findNavController().navigate(
            LikePlayerFragmentDirections.actionLikePlayerFragmentToProfileManagementFragment(
                profileDto = profileDto,
                isPlayer = true,
                isFromSearchResult = true
            )
        )
    }
}
