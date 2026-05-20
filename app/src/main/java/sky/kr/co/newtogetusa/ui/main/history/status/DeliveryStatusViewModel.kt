package sky.kr.co.newtogetusa.ui.main.history.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryStatusLogDto
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryStatusViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _items = MutableStateFlow<List<DeliveryStatusLogDto>>(emptyList())
    val items: StateFlow<List<DeliveryStatusLogDto>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun load(deliveryId: Long) = viewModelScope.launch {
        if (deliveryId <= 0L) return@launch

        _isLoading.value = true
        when (val res = deliveryRepository.getDeliveryStatusList(deliveryId)) {
            is ResultWrapper.Success -> _items.value = res.data
            else -> _event.value = Event.LoadFailed
        }
        _isLoading.value = false
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    sealed class Event {
        object Back : Event()
        object LoadFailed : Event()
    }
}
