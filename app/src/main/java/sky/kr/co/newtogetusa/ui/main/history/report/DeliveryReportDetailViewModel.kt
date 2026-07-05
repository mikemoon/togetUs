package sky.kr.co.newtogetusa.ui.main.history.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryReportDetailViewModel @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val userRepository: UserRepository,
) : BaseViewModel(baseViewModelFactory.create()) {

    val inputTextCountFlow = MutableStateFlow("0/300")
    val canSubmitFlow = MutableStateFlow(false)

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onTextChanged(text: CharSequence) {
        val value = text.toString()
        inputTextCountFlow.value = "${value.length}/300"
        canSubmitFlow.value = value.length > 5
    }

    fun report(userId: Int, reason: String, content: String) = viewModelScope.launch {
        if (userId <= 0 || content.length <= 5) {
            _event.value = Event.ReportFailed
            return@launch
        }

        val reportContent = "[$reason]\n${content.trim()}"
        when (val result = userRepository.postReportUser(
            userId,
            hashMapOf("report_cd" to "REPORT", "content" to reportContent)
        )) {
            is ResultWrapper.Success -> _event.value = Event.ReportCompleted
            is ResultWrapper.GenericError -> {
                if (result.code == "403" && result.message.orEmpty().contains("이미 신고")) {
                    _event.value = Event.ReportCompleted
                } else {
                    _event.value = Event.ReportFailed
                }
            }
            else -> _event.value = Event.ReportFailed
        }
    }

    sealed class Event {
        object ReportCompleted : Event()
        object ReportFailed : Event()
    }
}
