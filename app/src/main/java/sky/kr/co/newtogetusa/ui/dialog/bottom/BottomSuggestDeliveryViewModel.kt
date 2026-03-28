package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BottomSuggestDeliveryViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepo: DeliveryRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _requestItems = MutableStateFlow<List<BottomSuggestDeliveryDialog.SuggestRequestItem>>(emptyList())
    val requestItems: StateFlow<List<BottomSuggestDeliveryDialog.SuggestRequestItem>> = _requestItems

    fun loadRegisteredDeliveries() = viewModelScope.launch {
        when (val res = deliveryRepo.postDeliverySearch(DeliverySearchReq(type = "DELIVERY|MATCH", title = "", page_no = 0))) {
            is ResultWrapper.Success -> {
                _requestItems.value = res.data.deliveries
                    .filter { it.status_cd.startsWith("MATCH") }
                    .map {
                        BottomSuggestDeliveryDialog.SuggestRequestItem(
                            deliveryId = it.delivery_id,
                            title = it.title,
                            routeText = "${it.depart_address} - ${it.dest_address}"
                        )
                    }
            }
            else -> {
                _requestItems.value = emptyList()
            }
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Close : Event()
        object Confirm : Event()
    }
}