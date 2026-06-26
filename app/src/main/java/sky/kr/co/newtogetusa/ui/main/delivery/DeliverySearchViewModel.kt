package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.inputmethod.EditorInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.RecentSearchStore
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartViewModel.SearchMode
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliverySearchViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                  private val recentSearchStore: RecentSearchStore,
                                                  private val kakaoLocalRepository: KakaoLocalRepository):
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    var isStart = MutableStateFlow(true)
    var isInternational = MutableStateFlow(false)

    val recentSearchList = recentSearchStore.recentSearchFlow.asLiveData()

    val searchAddress = MutableStateFlow("")

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(q: String) { _query.value = q }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        searchAddress.value = s.toString()
        //setQuery(s.toString())
    }

    fun onSearchClick() {
        val keyword = searchAddress.value.trim()
        if (keyword.isEmpty()) return

        setQuery(keyword)
        viewModelScope.launch {
            recentSearchStore.addSearchKeyword(keyword)
        }
    }

    fun setVoiceSearchText(text: String) {
        val keyword = text.trim()
        if (keyword.isEmpty()) return
        searchAddress.value = keyword
        setQuery(keyword)
        viewModelScope.launch {
            recentSearchStore.addSearchKeyword(keyword)
        }
    }

    /** 최근 검색어 개별 삭제 */
    fun removeKeyword(keyword: String) {
        viewModelScope.launch {
            recentSearchStore.removeKeyword(keyword)
        }
    }

    /** 전체 삭제 */
    fun clearRecentSearch() {
        viewModelScope.launch {
            recentSearchStore.clearAll()
        }
    }

    fun onSearchIme(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            onSearchClick()
            return true
        }
        return false
    }

    fun onDeleteSearchText(){
        searchAddress.value = ""
    }

    // 위치 기반 키워드 검색 옵션

    private val _mode = MutableStateFlow(SearchMode.KEYWORD)
    var centerLat: Double? = null
    var centerLng: Double? = null
    var radius: Int? = 5000
    var sort: String = "accuracy" // "distance"로 바꾸면 centerLat/Lng 필수

    @OptIn(FlowPreview::class)
    val results: Flow<PagingData<KakaoSearchModel>> =
        combine(_mode, _query.debounce(300).map { it.trim() }.distinctUntilChanged()) { m, q ->
            m to q
        }.filter { (_, q) -> q.length >= 2 }
            .flatMapLatest { (m, q) ->
                when (m) {
                    SearchMode.ADDRESS -> kakaoLocalRepository.searchAddressPagingFlow(q, analyzeType = "similar")
                    SearchMode.KEYWORD -> kakaoLocalRepository.searchKeywordPagingFlow(
                        query = q,
                        centerLat = centerLat,
                        centerLng = centerLng,
                        radius = radius,
                        sort = sort
                    )
                }
            }.cachedIn(viewModelScope)

    private val _selectedAddress = SingleLiveEvent<KakaoSearchModel>()
    val selectedAddress: LiveData<KakaoSearchModel> = _selectedAddress
    fun onKakaoAddressClick(kakaoSearchModel: KakaoSearchModel) {
        _selectedAddress.value = kakaoSearchModel
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object VoiceSearch : Event()
    }
}
