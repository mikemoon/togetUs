package sky.kr.co.newtogetusa.ui.dialog.bottom.player

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BottomDeliveryApplyVM @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _agreeWarning = MutableStateFlow(false)
    val agreeWarning: StateFlow<Boolean> = _agreeWarning

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onAgreeChanged(isChecked: Boolean) {
        _agreeWarning.value = isChecked
    }

    fun onCloseClick() {
        _event.value = Event.Close
    }

    fun onApplyClick() {
        if (!_agreeWarning.value) return
        _event.value = Event.Confirm
    }

    sealed class Event {
        object Close : Event()
        object Confirm : Event()
    }
}