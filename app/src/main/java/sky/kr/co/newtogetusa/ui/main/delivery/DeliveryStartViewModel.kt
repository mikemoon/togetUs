package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.AddressSearchRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryStartViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val addressSearchRepository: AddressSearchRepository) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val searchAddress = MutableStateFlow("")
    fun searchAddress() = viewModelScope.launch {
        addressSearchRepository.searchAddress(
            query =  searchAddress.value
        )
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        searchAddress.value = s.toString()
        searchAddress()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
    }
}