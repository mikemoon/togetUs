package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

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
import kotlinx.coroutines.flow.combine
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
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartViewModel.SearchMode
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerJoinSearchVM @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
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

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(q: String) {
        _query.value = q
    }

    val searchAddress = MutableStateFlow("")
    val searchStep = MutableStateFlow<SearchStep>(SearchStep.NONE)


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
    fun onKakaoAddressClick(kakaoSearchModel: KakaoSearchModel) {
        _selectedAddress.value = kakaoSearchModel
        enableSelectedComplete.value = !kakaoSearchModel.name.isNullOrBlank()
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        searchAddress.value = s.toString()
        setQuery(s.toString())
    }

    val isEditMode = MutableStateFlow(true)
    fun setEditMode(isEditMode : Boolean){
        this.isEditMode.value = isEditMode
    }

    val areaRadius = MutableStateFlow(3) // 기본 3km

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
        object SearchFromMap : Event()

        object SelectedComplete : Event()
    }

    enum class SearchStep {
        NONE, SEARCH, AREA_SET
    }

}