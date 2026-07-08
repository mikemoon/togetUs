package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryPickupViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory): BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isImmediately = MutableStateFlow(true)
    val isFaceToFace = MutableStateFlow(true)
    val pickupDate = MutableStateFlow("")
    val pickupTime = MutableStateFlow("")

    val isConfirmButtonEnable: StateFlow<Boolean> =
        combine(isImmediately, pickupDate, pickupTime) { isImmediately, date, time ->
            isImmediately || (date.isNotBlank() && time.isNotBlank())
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object SelectDate : Event()
        object SelectTime : Event()
    }
}
