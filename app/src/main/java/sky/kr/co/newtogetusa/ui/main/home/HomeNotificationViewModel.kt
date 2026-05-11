package sky.kr.co.newtogetusa.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.my.NotificationDto
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeNotificationViewModel @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val myRepository: MyRepository
) : BaseViewModel(baseViewModelFactory.create()) {

    val notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val unreadCount = MutableStateFlow(0)
    val isLoading = MutableStateFlow(false)
    val isEmpty = MutableStateFlow(false)

    init {
        getNotifications()
    }

    fun getNotifications(pageNo: Int = 0, pageSize: Int = 20) {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = myRepository.getNotifications(pageNo, pageSize)) {
                is ResultWrapper.Success -> {
                    notifications.value = result.data.notifications
                    unreadCount.value = result.data.unreadCount
                    isEmpty.value = result.data.notifications.isEmpty()
                }
                is ResultWrapper.GenericError -> {
                    Timber.e("notification load failed ${result.code} ${result.message}")
                }
                ResultWrapper.NetworkError -> {
                    Timber.e("notification load network error")
                }
            }
            isLoading.value = false
        }
    }

    fun readAllNotifications(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            when (myRepository.putNotificationsReadAll()) {
                is ResultWrapper.Success -> {
                    markAllRead()
                    onComplete(true)
                }
                else -> onComplete(false)
            }
        }
    }

    fun readNotification(notification: NotificationDto, onComplete: (Boolean) -> Unit = {}) {
        if (!notification.isUnread) return

        viewModelScope.launch {
            when (myRepository.putNotificationRead(notification.notificationId)) {
                is ResultWrapper.Success -> {
                    markRead(notification.notificationId)
                    onComplete(true)
                }
                else -> onComplete(false)
            }
        }
    }

    private fun markAllRead() {
        notifications.value = notifications.value.map { it.copy(readYn = "Y") }
        unreadCount.value = 0
    }

    private fun markRead(notificationId: Long) {
        notifications.value = notifications.value.map { notification ->
            if (notification.notificationId == notificationId) {
                notification.copy(readYn = "Y")
            } else {
                notification
            }
        }
        unreadCount.value = notifications.value.count { it.isUnread }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object Setting : Event()
        object ReadAll : Event()
    }
}
