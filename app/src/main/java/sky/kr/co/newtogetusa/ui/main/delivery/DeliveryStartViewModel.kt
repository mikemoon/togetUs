package sky.kr.co.newtogetusa.ui.main.delivery

import android.location.Geocoder
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
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.TogetUs
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoSearchAddressResponse
import sky.kr.co.newtogetusa.repository.AddressSearchRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.ln

@HiltViewModel
class DeliveryStartViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val addressSearchRepository: AddressSearchRepository,
    private val kakaoLocalRepository: KakaoLocalRepository) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

        var isStart = true
    private val _mode = MutableStateFlow(SearchMode.KEYWORD)

    val searchAddress = MutableStateFlow("")
    fun searchAddress() = viewModelScope.launch {
        addressSearchRepository.searchAddress(
            query =  searchAddress.value
        )
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 사용처에서 setQuery로 값 바꾸면 자동으로 새 Paging 시작
    /*val results: Flow<PagingData<KakaoSearchAddressResponse.Document>> =
        _query
            .debounce(300)                // 타이핑 디바운스
            .map { it.trim() }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .flatMapLatest { q ->
                kakaoLocalRepository.searchAddressPagingFlow(query = q, analyzeType = "similar")
            }
            .cachedIn(viewModelScope)*/

    fun setQuery(q: String) { _query.value = q }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        searchAddress.value = s.toString()
        setQuery(s.toString())
        //searchAddress()
    }

    // 위치 기반 키워드 검색 옵션
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
    fun onKakaoAddressClick(name : String,
                            lat : Double?,
                            lng : Double?,
                            source : String?,
                            subtitle : String?,
                            roadAddress : String?) {
        _selectedAddress.value = KakaoSearchModel(name = name, lat = lat, lng =  lng,
            source = source, subtitle = subtitle, roadAddress = roadAddress)
    }

    fun setSelectedAddress(kakaoSearchModel: KakaoSearchModel) {
        _selectedAddress.value = kakaoSearchModel
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object FindAddressFromMap : Event()
    }

    enum class SearchMode { ADDRESS, KEYWORD }
}