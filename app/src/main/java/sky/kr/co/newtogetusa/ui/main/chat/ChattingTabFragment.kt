package sky.kr.co.newtogetusa.ui.main.chat

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingBinding
import sky.kr.co.newtogetusa.ui.MainViewModel
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingTabFragment : BaseFragment<FragmentChattingBinding, ChattingTabViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_chatting
    override val viewModel: ChattingTabViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var isTabSelectionReady = false

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (!isTabSelectionReady) return
            viewModel.selectTopTab(position)
            viewModel.refreshRoomsForTab(isPlayer = position == PLAYER_TAB_POSITION)
        }
    }



    override fun init() {
        super.init()

        with(dataBinding.vPager){
            adapter = ChatViewPagerAdapter(this@ChattingTabFragment)
            isUserInputEnabled = false
        }

        TabLayoutMediator(dataBinding.tab, dataBinding.vPager, false, false){ tab, position ->
            tab.text = when(position){
                0 -> {
                    getString(R.string.player)
                }
                1 ->{
                    getString(R.string.user)
                }
                else -> {
                    ""
                }
            }
        }.attach()
        dataBinding.vPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPlayerApproval()
        mainViewModel.refreshChatUnread()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatTabUiState.collectLatest { state ->
                    applyChatTabState(state)
                }
            }
        }
    }

    private fun applyChatTabState(state: ChattingTabViewModel.ChatTabUiState) {
        val showTopTab = state.isApprovedPlayer
        dataBinding.tab.isVisible = showTopTab
        dataBinding.vDivider.isVisible = showTopTab

        val targetPosition = when {
            showTopTab && viewModel.selectedTopTabPosition.value != null ->
                viewModel.selectedTopTabPosition.value!!
            showTopTab && state.isPlayerMode -> PLAYER_TAB_POSITION
            else -> USER_TAB_POSITION
        }
        if (dataBinding.vPager.currentItem != targetPosition) {
            dataBinding.vPager.setCurrentItem(targetPosition, false)
        }
        isTabSelectionReady = true
    }

    override fun onDestroyView() {
        dataBinding.vPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    companion object {
        private const val PLAYER_TAB_POSITION = 0
        private const val USER_TAB_POSITION = 1
    }

}
