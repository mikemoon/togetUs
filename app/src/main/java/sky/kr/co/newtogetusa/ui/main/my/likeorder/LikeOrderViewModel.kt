package sky.kr.co.newtogetusa.ui.main.my.likeorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LikeOrderViewModel @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository
) : BaseViewModel(baseViewModelFactory.create()) {

    val selectedStatus = MutableStateFlow(Status.ALL)

    val deliveryPagingData: Flow<PagingData<DeliverySummaryDto>> = selectedStatus
        .flatMapLatest { status ->
            deliveryRepository.getPlayerDeliveryLikePagingFlow(status.apiValue, "")
        }
        .cachedIn(viewModelScope)

    fun selectStatus(status: Status) {
        selectedStatus.value = status
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    enum class Status(val label: String, val apiValue: String) {
        ALL("전체", "ALL"),
        PROGRESS("거래 진행", "ING"),
        COMPLETE("거래 종료", "DONE")
    }

    sealed class Event {
        object Back : Event()
    }
}
