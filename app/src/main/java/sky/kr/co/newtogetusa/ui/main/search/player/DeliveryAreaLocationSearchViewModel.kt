package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryAreaLocationSearchViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val kakaoLocalRepository: KakaoLocalRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchStep = MutableStateFlow(SearchStep.NONE)
    val selectedAddress = MutableStateFlow<KakaoSearchModel?>(null)
    val areaRadius = MutableStateFlow(3)
    val isEditMode = MutableStateFlow(true)

    @OptIn(FlowPreview::class)
    val results: Flow<PagingData<KakaoSearchModel>> =
        _query
            .debounce(300)
            .map { it.trim() }
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .flatMapLatest { q ->
                kakaoLocalRepository.searchKeywordPagingFlow(query = q)
            }
            .cachedIn(viewModelScope)

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _query.value = s.toString()
    }

    fun onDeleteSearchText() {
        _query.value = ""
        searchStep.value = SearchStep.NONE
    }

    fun onAddressSelected(model: KakaoSearchModel) {
        selectedAddress.value = model
        searchStep.value = SearchStep.AREA_SET
        isEditMode.value = false
    }

    fun onModifyClick() {
        isEditMode.value = true
        searchStep.value = SearchStep.NONE
    }

    fun setAreaRadius(value: Float) {
        areaRadius.value = value.toInt()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object SelectedComplete : Event()
    }

    enum class SearchStep {
        NONE, SEARCH, AREA_SET
    }
}