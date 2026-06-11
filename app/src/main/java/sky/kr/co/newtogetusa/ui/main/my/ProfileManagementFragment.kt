package sky.kr.co.newtogetusa.ui.main.my

import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentProfileManagementBinding
import sky.kr.co.newtogetusa.databinding.TabMyProfileCustomBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomProfileMoreDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomSuggestDeliveryDialog
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ProfileManagementFragment : BaseFragment<FragmentProfileManagementBinding, ProfileManagementViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_management
    override val viewModel: ProfileManagementViewModel by viewModels()
    private val args : ProfileManagementFragmentArgs by navArgs()

    var profileDto : ProfileDto? = null
    var isPlayerMode  = false
    private var isFromSearchResult = false
    private var customTabBinding0:TabMyProfileCustomBinding? = null
    private var customTabBinding1:TabMyProfileCustomBinding? = null

    override fun init() {
        super.init()
        isPlayerMode = args.isPlayer
        isFromSearchResult = args.isFromSearchResult
        profileDto = args.profileDto
        viewModel.profileDto.value = profileDto
        dataBinding.profile = profileDto
        dataBinding.isFromSearchResult = isFromSearchResult

        if (isPlayerMode) {
            viewModel.getPlayerProfile {
                applyPlayerProfile(it)
            }
        } else if (!isFromSearchResult) {
            viewModel.getMyProfile {
                viewModel.profileDto.value = it
                dataBinding.profile = it
                profileDto = it
                dataBinding.tvScore.text = "${it.evaluation.start_average} (${it.review_count})"
            }
        } else {
            profileDto?.let {
                dataBinding.tvScore.text = "${it.evaluation.start_average} (${it.review_count})"
            }
        }

        dataBinding.viewPager.apply {
            isUserInputEnabled = false
            adapter = ProfileManagementPagerAdapter(
                childFragmentManager,
                viewLifecycleOwner.lifecycle,
                isPlayerMode,
                isFromSearchResult
            )
        }

        TabLayoutMediator(dataBinding.tabLayout, dataBinding.viewPager) { tab, pos ->
            tab.text = if (pos == 0) "소개" else "받은 후기"
        }.attach()
        dataBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                dataBinding.viewPager.setCurrentItem(tab.position, /* smoothScroll = */ false)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        if(!isPlayerMode){
            customTabBinding0 = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.tab_my_profile_custom, dataBinding.tabLayout, false)
            customTabBinding0?.tvTitle?.text = "동행 요청"
            dataBinding.tabLayout.getTabAt(0)?.customView = customTabBinding0?.root
            updateDeliveryRequestCount(0)
            viewModel.fetchDeliveryRequestTotalCount()
        }
        customTabBinding1 = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.tab_my_profile_custom, dataBinding.tabLayout, false)
        dataBinding.tabLayout.getTabAt(1)?.customView = customTabBinding1?.root
        updateReviewCount(0)
        viewModel.getPlayerReviews()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ProfileManagementViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ProfileManagementViewModel.Event.ModifyProfile -> {
                    findNavController().navigate(
                        ProfileManagementFragmentDirections.actionProfileManagementFragmentToModifyProfileFragment(
                            profileDto = profileDto,
                            isPlayer = isPlayerMode
                        )
                    )
                }
                is ProfileManagementViewModel.Event.PasswordSet -> {
                    findNavController().navigate(R.id.action_profileManagementFragment_to_passwordSetFragment)
                }
                is ProfileManagementViewModel.Event.SuggestDelivery -> {
                    showSuggestBottomDialog()
                }
                ProfileManagementViewModel.Event.More -> {
                    showProfileMoreBottomDialog()
                }
                ProfileManagementViewModel.Event.ToggleLike -> {
                    viewModel.togglePlayerLike()
                }
            }
        }

        viewModel.suggestDeliveryResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                requireContext().toast("제안하기 요청이 전송되었습니다.")
            } else {
                requireContext().toast("제안하기 요청 전송에 실패했습니다.")
            }
        }

        if (!isPlayerMode) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.deliveryRequestTotalCount.collectLatest {
                        updateDeliveryRequestCount(it)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playerReviewCount.collectLatest {
                        updateReviewCount(it)
                    }
                }
                launch {
                    viewModel.isPlayerLiked.collectLatest { isLiked ->
                        dataBinding.ivHeart.setImageResource(
                            if (isLiked) R.drawable.heart_fill_primary else R.drawable.heart_line
                        )
                    }
                }
                launch {
                    viewModel.profileActionMessage.collectLatest { message ->
                        requireContext().toast(message)
                    }
                }
            }
        }
    }

    private fun showSuggestBottomDialog() {
        val nickname = profileDto?.user?.nickname.orEmpty()
        BottomSuggestDeliveryDialog().apply {
            this.nickname = nickname
            selectedRequestCallback = { selectedItem ->
                this@ProfileManagementFragment.viewModel.requestSuggestDelivery(selectedItem.deliveryId)
            }
        }.show(parentFragmentManager, "BottomSuggestDeliveryDialog")
    }

    private fun showProfileMoreBottomDialog() {
        dialogFragmentShow(
            childFragmentManager,
            BottomProfileMoreDialog().apply {
                blockAction = {
                    dialogFragmentShow(
                        requireActivity().supportFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "차단하기",
                            msg = "이 사용자를 차단하시겠어요?",
                            leftBtn = "취소",
                            rightBtn = "확인"
                        ).onRightBtn {
                            this@ProfileManagementFragment.viewModel.blockPlayer()
                        }
                    )
                }
                reportAction = {
                    dialogFragmentShow(
                        requireActivity().supportFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "신고하기",
                            msg = "이 사용자를 신고하시겠어요?",
                            leftBtn = "취소",
                            rightBtn = "확인"
                        ).onRightBtn {
                            this@ProfileManagementFragment.viewModel.reportPlayer()
                        }
                    )
                }
            }
        )
    }

    private fun updateDeliveryRequestCount(count: Int){
        customTabBinding0?.tvCount?.text = count.toString()
    }

    private fun updateReviewCount(count:Int){
        customTabBinding1?.tvCount?.text = count.toString()
    }

    private fun applyPlayerProfile(playerProfile: PlayerProfileDto) {
        val currentProfile = profileDto ?: return
        val reviewCount = playerProfile.rating?.review_count
            ?: playerProfile.evaluation?.review_count
            ?: currentProfile.review_count
        val starAverage = playerProfile.evaluation?.start_average
            ?: playerProfile.rating?.star_average?.toInt()
            ?: currentProfile.evaluation.start_average
        val cancelCount = playerProfile.evaluation?.cancel_count
            ?: playerProfile.rating?.cancel_count
            ?: currentProfile.evaluation.cancel_count

        val updatedProfile = currentProfile.copy(
            user = currentProfile.user.copy(
                player_id = playerProfile.player.player_id,
                nickname = playerProfile.player.nickname,
                profile_image = playerProfile.player.profile_image,
                enable = playerProfile.player.enable
            ),
            evaluation = currentProfile.evaluation.copy(
                start_average = starAverage,
                cancel_count = cancelCount
            ),
            review_count = reviewCount
        )

        viewModel.profileDto.value = updatedProfile
        viewModel.setPlayerLiked(playerProfile.is_like == true)
        profileDto = updatedProfile
        dataBinding.profile = updatedProfile
        dataBinding.tvScore.text = "$starAverage ($reviewCount)"
    }
}
