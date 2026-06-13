package sky.kr.co.newtogetusa.ui.main.my.term

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.repository.AuthRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class TermViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val authRepository: AuthRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create())  {

    private val _terms = MutableStateFlow<List<TermMeta>>(emptyList())
    val terms = _terms.asStateFlow()

    init {
        getTerms()
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    private fun getTerms() = viewModelScope.launch {
        when (val response = authRepository.getTerms()) {
            is ResultWrapper.Success -> _terms.value = response.data
            else -> Unit
        }
    }

    fun findTerm(type: TermType): TermMeta? {
        return terms.value.firstOrNull { term ->
            val text = "${term.code} ${term.cate} ${term.name}".lowercase()
            type.keywords.any { keyword -> text.contains(keyword) }
        }
    }

    sealed class Event {
        object Back : Event()
        object UseTerm : Event()
        object PrivacyTerm : Event()
        object LocationTerm : Event()
    }

    enum class TermType(
        val title: String,
        val fallbackContent: String,
        val keywords: List<String>
    ) {
        USE(
            title = "이용약관",
            fallbackContent = "투겟어스 서비스 이용과 관련된 기본 약관입니다.",
            keywords = listOf("이용", "서비스", "use", "term")
        ),
        PRIVACY(
            title = "개인정보 처리방침",
            fallbackContent = "투겟어스 개인정보 처리방침입니다.",
            keywords = listOf("개인정보", "privacy")
        ),
        LOCATION(
            title = "위치정보 이용 동의",
            fallbackContent = "위치정보 수집 및 이용에 대한 안내입니다.",
            keywords = listOf("위치", "location")
        )
    }
}
