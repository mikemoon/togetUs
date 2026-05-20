package sky.kr.co.newtogetusa.ui.main.history.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryReviewDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryReviewRequest
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryReviewViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository,
    private val configRepository: ConfigRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val scoreFlow = MutableStateFlow(0)
    val reasonOptions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val selectedCodes = MutableStateFlow<Set<String>>(emptySet())
    val receivedReview = MutableStateFlow<DeliveryReviewDto?>(null)
    val contentCountFlow = MutableStateFlow("0/100")
    val canSubmitFlow = MutableStateFlow(true)

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun load(deliveryId: Long, isPlayer: Boolean) = viewModelScope.launch {
        if (deliveryId > 0L) {
            when (val check = deliveryRepository.checkReview(deliveryId, isPlayer)) {
                is ResultWrapper.Success -> {
                    canSubmitFlow.value = check.data.canReview
                    if (!check.data.canReview) {
                        _event.value = Event.ReviewBlocked(check.data.message ?: "후기를 작성할 수 없습니다.")
                    }
                }
                else -> {}
            }
        }

        val res = if (isPlayer) configRepository.getUserReview() else configRepository.getPlayerReview()
        if (res is ResultWrapper.Success) {
            reasonOptions.value = res.data.map { it.code to it.name }
        }
    }

    fun loadReceivedReview(deliveryId: Long, isPlayer: Boolean) = viewModelScope.launch {
        when (val res = deliveryRepository.getReviewed(deliveryId, isPlayer)) {
            is ResultWrapper.Success -> receivedReview.value = res.data
            else -> _event.value = Event.LoadFailed
        }
    }

    fun setScore(score: Int) {
        scoreFlow.value = score.coerceIn(1, 5)
    }

    fun toggleCode(code: String) {
        selectedCodes.value = selectedCodes.value.toMutableSet().apply {
            if (contains(code)) remove(code) else add(code)
        }
    }

    fun onContentChanged(text: CharSequence) {
        contentCountFlow.value = "${text.length}/100"
    }

    fun submit(deliveryId: Long, isPlayer: Boolean, contents: String) = viewModelScope.launch {
        if (!canSubmitFlow.value) {
            _event.value = Event.ReviewBlocked("후기를 작성할 수 없습니다.")
            return@launch
        }

        if (deliveryId <= 0L || scoreFlow.value <= 0) {
            _event.value = Event.Invalid
            return@launch
        }

        loadingState.value = true
        val request = DeliveryReviewRequest(
            stars = scoreFlow.value,
            contents = contents.trim(),
            items = selectedCodes.value.toList(),
        )
        when (deliveryRepository.writeReview(deliveryId, isPlayer, request)) {
            is ResultWrapper.Success -> _event.value = Event.SubmitSuccess
            else -> _event.value = Event.SubmitFailed
        }
        loadingState.value = false
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    sealed class Event {
        object Back : Event()
        object Invalid : Event()
        object SubmitSuccess : Event()
        object SubmitFailed : Event()
        object LoadFailed : Event()
        data class ReviewBlocked(val message: String) : Event()
    }
}
