package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

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
class PlayerJoinSearchVM @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val recentSearchStore: RecentSearchStore,
    private val kakaoLocalRepository: KakaoLocalRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _mode = MutableStateFlow(SearchMode.KEYWORD)

    val isStartArea = MutableStateFlow(true)
    val isSecondary = MutableStateFlow(false)

    val enableSelectedComplete = MutableStateFlow(false)

    var centerLat: Double? = null
    var centerLng: Double? = null
    var radius: Int? = 5000
    var sort: String = "accuracy" // "distance"로 바꾸면 centerLat/Lng 필수

    // 최근 검색어
    val recentSearchList = recentSearchStore.recentSearchFlow.asLiveData()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(q: String) {
        _query.value = q
    }

    val searchAddress = MutableStateFlow("")
    val searchStep = MutableStateFlow<SearchStep>(SearchStep.RECENT)


    @OptIn(FlowPreview::class)
    val results: Flow<PagingData<KakaoSearchModel>> =
        combine(_mode, _query.debounce(300).map { it.trim() }.distinctUntilChanged()) { m, q ->
            m to q
        }.filter { (_, q) -> q.length >= 2 }
            .flatMapLatest { (m, q) ->
                when (m) {
                    SearchMode.ADDRESS -> kakaoLocalRepository.searchAddressPagingFlow(
                        q,
                        analyzeType = "similar"
                    )

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

    /** 현재 선택된 주소를 보관하는 상태 변수 (SingleLiveEvent와 별도로 유지) */
    var currentSelectedAddress: KakaoSearchModel? = null
        private set

    fun onKakaoAddressClick(kakaoSearchModel: KakaoSearchModel) {
        _selectedAddress.value = kakaoSearchModel
        currentSelectedAddress = kakaoSearchModel
        enableSelectedComplete.value = !kakaoSearchModel.name.isNullOrBlank()

        // 최근 검색어에 저장
        val keyword = kakaoSearchModel.name
        if (keyword.isNotBlank()) {
            viewModelScope.launch {
                recentSearchStore.addSearchKeyword(keyword)
            }
        }
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        searchAddress.value = s.toString()
        val text = s.toString()
        if (text.isNotEmpty()) {
            setQuery(text)
            searchStep.value = SearchStep.SEARCH
        } else {
            setQuery("")
            searchStep.value = SearchStep.RECENT
        }
    }

    fun onSearchClick() {
        val keyword = searchAddress.value.trim()
        if (keyword.isEmpty()) return

        setQuery(keyword)
        searchStep.value = SearchStep.SEARCH
        viewModelScope.launch {
            recentSearchStore.addSearchKeyword(keyword)
        }
    }

    fun setVoiceSearchText(text: String) {
        val keyword = text.trim()
        if (keyword.isEmpty()) return
        searchAddress.value = keyword
        setQuery(keyword)
        searchStep.value = SearchStep.SEARCH
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

    /** 최근 검색어 전체 삭제 */
    fun clearRecentSearch() {
        viewModelScope.launch {
            recentSearchStore.clearAll()
        }
    }

    fun onDeleteSearchText() {
        searchAddress.value = ""
        setQuery("")
        searchStep.value = SearchStep.RECENT
    }

    fun onModifyClick() {
        setEditMode(true)
        searchStep.value = SearchStep.RECENT
    }

    val isEditMode = MutableStateFlow(true)
    fun setEditMode(isEditMode : Boolean){
        this.isEditMode.value = isEditMode
    }

    val areaRadius = MutableStateFlow(3) // 기본 3km

    fun setAreaRadius(value: Float) {
        areaRadius.value = value.toInt().coerceIn(1, 5)
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object SearchFromMap : Event()
        object VoiceSearch : Event()
        object SelectedComplete : Event()
    }

    enum class SearchStep {
        RECENT, SEARCH, AREA_SET
    }

}