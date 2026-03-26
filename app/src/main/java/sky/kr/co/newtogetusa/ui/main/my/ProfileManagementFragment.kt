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
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentProfileManagementBinding
import sky.kr.co.newtogetusa.databinding.TabMyProfileCustomBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
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
        dataBinding.profile = profileDto
        dataBinding.isFromSearchResult = isFromSearchResult

        if (!isFromSearchResult) {
            viewModel.getMyProfile {
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
            adapter = ProfileManagementPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, isPlayerMode)
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
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ProfileManagementViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ProfileManagementViewModel.Event.ModifyProfile -> {
                    findNavController().navigate(ProfileManagementFragmentDirections.actionProfileManagementFragmentToModifyProfileFragment(profileDto))
                }
                is ProfileManagementViewModel.Event.PasswordSet -> {
                    findNavController().navigate(R.id.action_profileManagementFragment_to_passwordSetFragment)
                }
                is ProfileManagementViewModel.Event.SuggestDelivery -> {
                    requireContext().toast("제안하기 기능을 준비중입니다.")
                }
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
    }

    private fun updateDeliveryRequestCount(count: Int){
        customTabBinding0?.tvCount?.text = count.toString()
    }

    private fun updateReviewCount(count:Int){
        customTabBinding1?.tvCount?.text = count.toString()
    }
}