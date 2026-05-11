package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryFeeAgreementViewModel @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory
) : BaseViewModel(baseViewModelFactory.create()) {

    val isAgreeChecked = MutableLiveData(false)

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onAgree() {
        isAgreeChecked.value = isAgreeChecked.value != true
    }

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object Confirm : Event()
    }
}
