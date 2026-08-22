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
    val sentReview = MutableStateFlow<DeliveryReviewDto?>(null)
    val contentCountFlow = MutableStateFlow("0/100")
    val titleFlow = MutableStateFlow("동행요청은 만족스러우셨나요?")
    val sentReviewTitleFlow = MutableStateFlow("거래 후기를 남겼어요.")
    val canSubmitFlow = MutableStateFlow(false)
    private var canReview = true
    private var reviewItemNameMap: Map<String, String>? = null

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun load(deliveryId: Long, isPlayer: Boolean) = viewModelScope.launch {
        if (deliveryId > 0L) {
            when (val detail = deliveryRepository.getDeliveryDetail(deliveryId)) {
                is ResultWrapper.Success -> {
                    val nickname = if (isPlayer) {
                        detail.data.requester_rating.nickname
                    } else {
                        detail.data.player_profile?.nickname.orEmpty()
                    }
                    titleFlow.value = if (nickname.isBlank()) {
                        "동행요청은 만족스러우셨나요?"
                    } else {
                        "${nickname}님의 동행요청은 만족스러우셨나요?"
                    }
                }
                else -> {}
            }
            when (val check = deliveryRepository.checkReview(deliveryId, isPlayer)) {
                is ResultWrapper.Success -> {
                    canReview = check.data.canReview
                    updateSubmitEnabled()
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
            is ResultWrapper.Success -> {
                val review = res.data.localizeReviewItems()
                if (review.hasReviewContent()) {
                    receivedReview.value = review
                } else {
                    _event.value = Event.ReceivedReviewNotFound("받은 후기를 찾을 수 없습니다.")
                }
            }
            is ResultWrapper.GenericError -> _event.value =
                Event.ReceivedReviewNotFound(res.message ?: "받은 후기를 찾을 수 없습니다.")
            else -> _event.value = Event.LoadFailed
        }
    }

    fun loadSentReview(deliveryId: Long, isPlayer: Boolean) = viewModelScope.launch {
        if (deliveryId > 0L) {
            when (val detail = deliveryRepository.getDeliveryDetail(deliveryId)) {
                is ResultWrapper.Success -> {
                    val nickname = if (isPlayer) {
                        detail.data.requester_rating.nickname
                    } else {
                        detail.data.player_profile?.nickname.orEmpty()
                    }
                    sentReviewTitleFlow.value = if (nickname.isBlank()) {
                        "거래 후기를 남겼어요."
                    } else {
                        "${nickname}님에게 거래 후기를 남겼어요."
                    }
                }
                else -> {}
            }
        }
        when (val res = deliveryRepository.getReview(deliveryId, isPlayer)) {
            is ResultWrapper.Success -> sentReview.value = res.data.localizeReviewItems()
            else -> _event.value = Event.LoadFailed
        }
    }

    fun setScore(score: Int) {
        scoreFlow.value = score.coerceIn(1, 5)
        updateSubmitEnabled()
    }

    fun toggleCode(code: String) {
        selectedCodes.value = selectedCodes.value.toMutableSet().apply {
            if (contains(code)) remove(code) else add(code)
        }
    }

    fun onContentChanged(text: CharSequence) {
        contentCountFlow.value = "${text.length}/100"
    }

    private fun updateSubmitEnabled() {
        canSubmitFlow.value = canReview && scoreFlow.value > 0
    }

    private suspend fun DeliveryReviewDto.localizeReviewItems(): DeliveryReviewDto {
        if (items.isEmpty()) return this

        val names = getReviewItemNameMap()
        return copy(items = items.map { code -> names[code] ?: code })
    }

    private fun DeliveryReviewDto.hasReviewContent(): Boolean {
        return reviewId > 0L ||
            stars > 0 ||
            !contents.isNullOrBlank() ||
            items.isNotEmpty() ||
            !nickname.isNullOrBlank() ||
            !profileImage.isNullOrBlank()
    }

    private suspend fun getReviewItemNameMap(): Map<String, String> {
        reviewItemNameMap?.let { return it }

        val names = mutableMapOf<String, String>()
        when (val res = configRepository.getPlayerReview()) {
            is ResultWrapper.Success -> names.putAll(res.data.associate { it.code to it.name })
            else -> Unit
        }
        when (val res = configRepository.getUserReview()) {
            is ResultWrapper.Success -> names.putAll(res.data.associate { it.code to it.name })
            else -> Unit
        }
        reviewItemNameMap = names
        return names
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
        data class ReceivedReviewNotFound(val message: String) : Event()
        data class ReviewBlocked(val message: String) : Event()
    }
}
