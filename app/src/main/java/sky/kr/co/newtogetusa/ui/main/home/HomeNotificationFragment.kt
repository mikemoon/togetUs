package sky.kr.co.newtogetusa.ui.main.home

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.my.NotificationDto
import sky.kr.co.newtogetusa.databinding.FragmentHomeNotificationBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class HomeNotificationFragment :
    BaseFragment<FragmentHomeNotificationBinding, HomeNotificationViewModel>() {

    override val layoutId: Int = R.layout.fragment_home_notification
    override val viewModel: HomeNotificationViewModel by viewModels()

    private val notificationAdapter = HomeNotificationAdapter { notification ->
        onNotificationClick(notification)
    }

    override fun init() {
        super.init()
        dataBinding.rvNotifications.adapter = notificationAdapter
        dataBinding.rvNotifications.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val itemCount = notificationAdapter.itemCount
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (itemCount > 0 && lastVisiblePosition >= itemCount - LOAD_MORE_THRESHOLD) {
                    viewModel.loadMoreNotifications()
                }
            }
        })
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notifications.collectLatest { notifications ->
                        notificationAdapter.setItems(notifications)
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                HomeNotificationViewModel.Event.Back -> findNavController().popBackStack()
                HomeNotificationViewModel.Event.Setting -> requireContext().toast("알림 설정 화면을 준비중입니다.")
                HomeNotificationViewModel.Event.ReadAll -> {
                    viewModel.readAllNotifications { success ->
                        requireContext().toast(
                            if (success) "알림을 모두 읽음 처리했습니다." else "알림 읽음 처리에 실패했습니다."
                        )
                    }
                }
            }
        }
    }

    private fun onNotificationClick(notification: NotificationDto) {
        viewModel.readNotification(notification) { success ->
            if (!success) {
                requireContext().toast("알림 읽음 처리에 실패했습니다.")
            }
        }
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 3
    }
}
