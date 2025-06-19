package sky.kr.co.newtogetusa.ui.main.search

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomChatMoreViewModel.Event
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isAreaSelectState = MutableStateFlow(true)
    fun onAreaClick(isArea:Boolean){
        isAreaSelectState.value = isArea
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