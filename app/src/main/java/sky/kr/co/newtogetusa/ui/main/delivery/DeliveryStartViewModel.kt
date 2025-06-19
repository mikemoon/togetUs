package sky.kr.co.newtogetusa.ui.main.delivery

import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.TogetUs
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.AddressSearchRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
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
        Timber.d("onTextChanged $s")
        searchAddress.value = s.toString()
        //searchAddress()
    }

    private val _selectedAddress = SingleLiveEvent<SearchResultModel>()
    val selectedAddress: LiveData<SearchResultModel> = _selectedAddress
    fun onAddressClick(address: SearchResultModel) {
        _selectedAddress.value = address
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