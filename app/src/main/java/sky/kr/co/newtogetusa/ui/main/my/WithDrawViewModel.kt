package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class WithDrawViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory):
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

        val withDrawStep = MutableStateFlow(1)

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object WithDraw : Event()
    }
}