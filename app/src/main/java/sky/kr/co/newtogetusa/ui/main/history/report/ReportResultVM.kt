package sky.kr.co.newtogetusa.ui.main.history.report

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ReportResultVM @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
) : BaseViewModel(baseViewModelFactory.create()) {

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onBackClick() {
        _event.value = Event.Back
    }

    fun onReceivedReviewClick() {
        _event.value = Event.OpenReceivedReview
    }

    sealed class Event {
        object Back : Event()
        object OpenReceivedReview : Event()
    }
}