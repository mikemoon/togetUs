package sky.kr.co.newtogetusa.ui.main.history.report

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ReportVM @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
) : BaseViewModel(baseViewModelFactory.create()) {

    val scoreFlow = MutableStateFlow(1)
    val contentCountFlow = MutableStateFlow("0/100")

    private val reasonOptions = listOf(
        "시간 약속을 잘 지켰어요.",
        "빠르게 응답했어요.",
        "매너가 좋았어요.",
        "물품 정보가 정확했어요.",
    )

    private val _selectedReasonsFlow = MutableStateFlow<Set<String>>(emptySet())
    val selectedReasonsFlow = _selectedReasonsFlow

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun setScore(score: Int) {
        scoreFlow.value = score.coerceIn(1, 5)
    }

    fun toggleReason(reason: String) {
        if (reason !in reasonOptions) return
        _selectedReasonsFlow.value = _selectedReasonsFlow.value.toMutableSet().apply {
            if (contains(reason)) remove(reason) else add(reason)
        }
    }

    fun onContentChanged(text: CharSequence) {
        contentCountFlow.value = "${text.length}/100"
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    fun onSubmit(message: String) {
        val trimmedMessage = message.trim()
        _event.value = Event.OpenResult(
            reasons = _selectedReasonsFlow.value.toList(),
            message = trimmedMessage.ifBlank { null },
            showBottomButton = trimmedMessage.isNotBlank(),
        )
    }

    sealed class Event {
        object Back : Event()
        data class OpenResult(
            val reasons: List<String>,
            val message: String?,
            val showBottomButton: Boolean,
        ) : Event()
    }
}