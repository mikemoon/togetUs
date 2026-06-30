package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.config.NotificationSettingDto
import sky.kr.co.newtogetusa.data.remote.request.config.NotificationSettingReq
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class MySettingViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val configRepository: ConfigRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    init {
        getAlarmSettings()
    }

    val alarmSettingState = MutableStateFlow<NotificationSettingDto?>(null)

    fun getAlarmSettings() = viewModelScope.launch {
        when (val res = configRepository.getAlarmSettings()) {
            is ResultWrapper.Success -> {
                if (res.data is NotificationSettingDto) {
                    alarmSettingState.value = res.data
                }
            }

            else -> {
            }
        }
    }

    fun setAlarmSettings(settings: NotificationSettingReq) = viewModelScope.launch {
        when (val res = configRepository.putAlarmSettings(
            settings
        )
        ) {
            is ResultWrapper.Success -> {

            }

            else -> {

            }
        }
    }

    fun updateAlarmSetting(update: (NotificationSettingDto) -> NotificationSettingDto) {
        val current = alarmSettingState.value ?: return
        val updated = update(current)
        alarmSettingState.value = updated
        setAlarmSettings(updated.toReq())
    }

    private fun NotificationSettingDto.toReq() = NotificationSettingReq(
        pushYn = pushYn.toYn(),
        chatYn = chatYn.toYn(),
        marketingYn = marketingYn.toYn(),
        deliveryYn = deliveryYn.toYn(),
        nightPushYn = nightPushYn.toYn()
    )

    private fun Boolean.toYn(): String = if (this) "Y" else "N"

    fun logout(callback: () -> Unit) = viewModelScope.launch {
        NaverIdLoginSDK.logout()
        dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, "")
        dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, "")
        dataStoreRepository.clearString(DataStoreKey.KEY_PROFILE)
        when (val res = configRepository.deletePushToken(
            hashMapOf(
                "device_token" to dataStoreRepository.getString(
                    DataStoreKey.KEY_FCM_TOKEN
                ), "device_type" to null
            )
        )) {
            is ResultWrapper.Success -> {
                if (res.data) {
                    callback()
                }
            }

            else -> {
            }
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object ManageProfile : Event()
        object DeliveryAlarm : Event()
        object ChattingAlarm : Event()
        object MarkettingAlarm : Event()
        object NightAlarm : Event()
        object BlockManagement : Event()
        object Logout : Event()
        object WithDraw : Event()
    }
}
