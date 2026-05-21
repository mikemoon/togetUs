package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class AccompanyCreditViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onBackClick() {
        _event.value = Event.Back
    }

    sealed class Event {
        object Back : Event()
    }
}
