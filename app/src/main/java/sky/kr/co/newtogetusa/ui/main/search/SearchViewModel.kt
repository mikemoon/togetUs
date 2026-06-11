package sky.kr.co.newtogetusa.ui.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isModePlayer = MutableStateFlow<Boolean?>(null)
        init {
            viewModelScope.launch {
                dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                    .collectLatest { isPlayerMode ->
                        isModePlayer.value = isPlayerMode
                    }
            }
        }

    private val isDepartAreaSelected = MutableStateFlow(false)
    private val isDestinationAreaSelected = MutableStateFlow(false)

    val enablePlayerSearch = combine(
        isDepartAreaSelected,
        isDestinationAreaSelected
    ) { isDepartSelected, isDestinationSelected ->
        isDepartSelected && isDestinationSelected
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false
    )

    fun setDepartAreaSelected(isSelected: Boolean) {
        isDepartAreaSelected.value = isSelected
    }

    fun setDestinationAreaSelected(isSelected: Boolean) {
        isDestinationAreaSelected.value = isSelected
    }


    private val _search = SingleLiveEvent<Boolean>()
    val search: LiveData<Boolean> = _search
    fun onSearch(){
        _search.value = true
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> get() = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event{
        object StartRegion: Event()
        object DestinationRegion: Event()
        object Search : Event()
    }
}
