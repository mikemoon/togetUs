package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.dto.users.DeliveryItem
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryRequestSearchViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val userRepository: UserRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isModePlayer = MutableStateFlow<Boolean?>(null)

    val searchCondition = MutableStateFlow(
        SearchCondition(
            myArea = false,
            departCd = emptyList(),
            destCd = emptyList(),
            sortType = SORT_TYPE_NEWEST,
            fee = 0,
            face2Face = null,
            immediately = IMMEDIATELY_ALL
        )
    )

    val deliveryRequestPagingData: Flow<PagingData<DeliveryItem>> = searchCondition
        .flatMapLatest { condition ->
            userRepository.getDeliveryRequestSearchPagingFlow(
                myArea = condition.myArea,
                departCd = condition.departCd,
                destCd = condition.destCd,
                sortType = condition.sortType,
                fee = condition.fee,
                face2Face = condition.face2Face,
                immediately = condition.immediately
            )
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                .collectLatest { isPlayerMode ->
                    isModePlayer.value = isPlayerMode
                }
        }
    }

    fun updateSortType(sortType: String) {
        searchCondition.value = searchCondition.value.copy(sortType = sortType)
    }

    fun updateFilterCondition(
        myArea: Boolean,
        face2Face: Boolean?,
        immediately: String
    ) {
        searchCondition.value = searchCondition.value.copy(
            myArea = myArea,
            face2Face = face2Face,
            immediately = immediately
        )
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Sort : Event()
        object Filter : Event()
        object AreaRequirement : Event()
    }

    data class SearchCondition(
        val myArea: Boolean,
        val departCd: List<String>,
        val destCd: List<String>,
        val sortType: String,
        val fee: Int,
        val face2Face: Boolean?,
        val immediately: String
    )

    companion object {
        const val SORT_TYPE_NEWEST = "NEWEST"
        const val SORT_TYPE_DEADLINE = "DEADLINE"
        const val IMMEDIATELY_ALL = "ALL"
    }
}