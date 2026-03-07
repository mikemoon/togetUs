package sky.kr.co.newtogetusa.ui.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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


    fun searchPlayers(
        departCd: List<String>,
        destCd: List<String>,
        sortType: String
    ): Flow<PagingData<PlayerDto>> {

        return playerRepository.searchPlayers(
            departCd,
            destCd,
            sortType
        ).cachedIn(viewModelScope)
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
}