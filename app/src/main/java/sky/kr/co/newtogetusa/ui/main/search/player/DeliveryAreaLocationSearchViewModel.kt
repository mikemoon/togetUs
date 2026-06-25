package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryAreaLocationSearchViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val kakaoLocalRepository: KakaoLocalRepository,
    private val configRepository: ConfigRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private var domesticAreas: List<RegionDto> = emptyList()
    private var domesticSubAreas: List<RegionDto> = emptyList()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchStep = MutableStateFlow(SearchStep.NONE)
    val selectedAddress = MutableStateFlow<KakaoSearchModel?>(null)
    val areaRadius = MutableStateFlow(3)
    val isEditMode = MutableStateFlow(true)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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

    init {
        fetchAreaConfigs()
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _query.value = s.toString()
    }

    fun onDeleteSearchText() {
        _query.value = ""
        searchStep.value = SearchStep.NONE
    }

    fun setVoiceSearchText(text: String) {
        _query.value = text
        searchStep.value = SearchStep.SEARCH
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

    fun getSelectedAreaCodes(): List<String> {
        val selected = selectedAddress.value ?: return emptyList()
        val address = selected.roadAddress?.takeIf { it.isNotBlank() } ?: selected.name
        val normalizedAddress = address.normalizeAreaText()

        val matchedDomestic = domesticAreas
            .filter { normalizedAddress.contains(it.name.normalizeAreaText()) }
            .sortedByDescending { it.name.length }

        val selectedDomestic = matchedDomestic.firstOrNull()
        if (selectedDomestic != null) {
            val selectedDomesticSub = domesticSubAreas
                .asSequence()
                .filter { it.cate == selectedDomestic.code }
                .filter { normalizedAddress.contains(it.name.normalizeAreaText()) }
                .maxByOrNull { it.name.length }

            return listOfNotNull(selectedDomestic.code, selectedDomesticSub?.code)
        }

        return emptyList()
    }

    private fun fetchAreaConfigs() = viewModelScope.launch {
        when (val domesticResponse = configRepository.getDomesticAreas()) {
            is ResultWrapper.Success -> domesticAreas = domesticResponse.data
            else -> Unit
        }

        when (val domesticSubResponse = configRepository.getDomesticSubAreas()) {
            is ResultWrapper.Success -> domesticSubAreas = domesticSubResponse.data
            else -> Unit
        }
    }

    private fun String.normalizeAreaText(): String = replace(" ", "")

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object VoiceSearch : Event()
        object SelectedComplete : Event()
    }

    enum class SearchStep {
        NONE, SEARCH, AREA_SET
    }
}
