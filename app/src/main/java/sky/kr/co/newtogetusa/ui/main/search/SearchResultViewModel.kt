package sky.kr.co.newtogetusa.ui.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val searchCondition = MutableStateFlow<SearchCondition?>(null)

    val playerPagingData: Flow<PagingData<PlayerDto>> = searchCondition
        .filterNotNull()
        .flatMapLatest { condition ->
            playerRepository.searchPlayers(
                departCd = condition.departCd,
                destCd = condition.destCd,
                sortType = condition.sortType
            )
        }
        .cachedIn(viewModelScope)

    fun updateSearchCondition(
        departCd: List<String>,
        destCd: List<String>,
        sortType: String
    ) {
        searchCondition.value = SearchCondition(
            departCd = departCd,
            destCd = destCd,
            sortType = sortType
        )
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> get() = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event{
        object Back: Event()
        object Filter : Event()
    }

    data class SearchCondition(
        val departCd: List<String>,
        val destCd: List<String>,
        val sortType: String
    )
}