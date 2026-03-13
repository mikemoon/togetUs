package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class MySettingViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()) {

    fun logout(callback: () -> Unit) = viewModelScope.launch {
        dataStoreRepository.putString(DataStoreKey.KEY_REFRESH_TOKEN, "")
        dataStoreRepository.putString(DataStoreKey.KEY_TOKEN, "")
        callback()
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
        object Logout : Event()
        object WithDraw : Event()
    }
}