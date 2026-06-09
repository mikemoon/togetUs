package sky.kr.co.newtogetusa.ui.main.chat

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
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
        dataBinding.vPager.setCurrentItem(USER_TAB_POSITION, false)
        dataBinding.tab.setOnTouchListener { _, _ -> true }

        for(i in 0 until dataBinding.tab.tabCount){
            dataBinding.tab.getTabAt(i)?.view?.apply {
                isClickable = false
                isLongClickable = false
                isEnabled = false
                setOnLongClickListener { true }
            }
        }

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

        val targetPosition = if (showTopTab && state.isPlayerMode) {
            PLAYER_TAB_POSITION
        } else {
            USER_TAB_POSITION
        }
        if (dataBinding.vPager.currentItem != targetPosition) {
            dataBinding.vPager.setCurrentItem(targetPosition, false)
        }
    }

    companion object {
        private const val PLAYER_TAB_POSITION = 0
        private const val USER_TAB_POSITION = 1
    }

}
