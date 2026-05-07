package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryDetailResponse
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import sky.kr.co.newtogetusa.utils.TextConvertUtil.formatPickupDateTime
import sky.kr.co.newtogetusa.utils.TextConvertUtil.formatWon
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class PlayerHistoryDetailViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepo: DeliveryRepository,
    private val configRepository: ConfigRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val deliveryDetail = MutableStateFlow<DeliveryDetailResponse?>(null)
    val mapShowState = MutableStateFlow<HomeTabViewModel.MapShow>(HomeTabViewModel.MapShow.LOCAL_IMAGE)

    private val productTypes = MutableStateFlow<List<BaseCommonDto>>(emptyList())
    private val productWeights = MutableStateFlow<List<BaseCommonDto>>(emptyList())
    private val productVolumes = MutableStateFlow<List<BaseCommonDto>>(emptyList())

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    init {
        getConfigProductType()
        getConfigProductWeight()
        getConfigProductVolume()
        decideMapProvider()
    }

    fun getDeliveryDetailInfo(deliveryId: Long) = viewModelScope.launch {
        when (val res = deliveryRepo.getDeliveryDetail(deliveryId)) {
            is ResultWrapper.Success -> deliveryDetail.value = res.data
            else -> {}
        }
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    fun onSecondaryClick() {
        val detail = deliveryDetail.value ?: return
        when (buttonState.value) {
            ButtonState.MatchBefore -> requestAsk(detail.delivery_id)
            ButtonState.DeliveryProgress -> _event.value = Event.Chat
            else -> Unit
        }
    }

    fun onPrimaryClick() {
        val detail = deliveryDetail.value ?: return
        when (buttonState.value) {
            ButtonState.MatchBefore -> _event.value = Event.ShowApplyDialog
            ButtonState.PickupReady -> requestPickupComplete(detail.delivery_id)
            ButtonState.DeliveryProgress -> requestDeliveryComplete(detail.delivery_id)
            ButtonState.Done -> _event.value = Event.OpenReport
            else -> Unit
        }
    }

    private fun requestAsk(deliveryId: Long) = viewModelScope.launch {
        /*when (deliveryRepo.putAsk(deliveryId)) {
            is ResultWrapper.Success -> _event.value = Event.ActionSuccess("문의가 전달되었어요.")
            else -> _event.value = Event.ActionFail
        }*/
    }

    private fun requestApply(deliveryId: Long) = viewModelScope.launch {
        when (deliveryRepo.putApply(deliveryId)) {
            is ResultWrapper.Success -> {
                _event.value = Event.ActionSuccess("지원이 완료되었어요.")
                getDeliveryDetailInfo(deliveryId)
            }
            else -> _event.value = Event.ActionFail
        }
    }

    private fun requestPickupComplete(deliveryId: Long) = viewModelScope.launch {
        /*when (deliveryRepo.putPickupComplete(deliveryId)) {
            is ResultWrapper.Success -> {
                _event.value = Event.ActionSuccess("픽업 완료 처리되었어요.")
                getDeliveryDetailInfo(deliveryId.toLong())
            }
            else -> _event.value = Event.ActionFail
        }*/
    }

    private fun requestDeliveryComplete(deliveryId: Long) = viewModelScope.launch {
        /*when (deliveryRepo.putDeliveryComplete(deliveryId)) {
            is ResultWrapper.Success -> {
                _event.value = Event.ActionSuccess("동행 완료 처리되었어요.")
                getDeliveryDetailInfo(deliveryId.toLong())
            }
            else -> _event.value = Event.ActionFail
        }*/
    }

    val buttonState = deliveryDetail.map { detail ->
        when (detail?.status_cd.orEmpty()) {
            "MATCH_BEFORE" -> ButtonState.MatchBefore
            "DELIVERY_BEFORE", "DELIVERY_WAIT", "DELIVERY_START", "PICKUP_START", "DELIVERY_DEPART" -> ButtonState.PickupReady
            "DELIVERY_ING" -> ButtonState.DeliveryProgress
            "DONE", "DONE_END" -> ButtonState.Done
            else -> ButtonState.Hidden
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ButtonState.Hidden
    )

    val deliveryData = combine(
        deliveryDetail,
        productTypes,
        productWeights,
        productVolumes,
    ) { detail, types, weights, volumes ->

        if (detail == null) return@combine null

        val typeName = types.firstOrNull { it.code == detail.product.type_cd }?.name.orEmpty()
        val weightName = weights.firstOrNull { it.code == detail.product.weight_cd }?.name.orEmpty()
        val volumeName = volumes.firstOrNull { it.code == detail.product.volume_cd }?.name.orEmpty()

        DeliverySummaryUiModel(
            title = detail.title,
            statusText = formatStatus(detail.status_cd),
            priceText = formatWon(detail.fee.fee_final),
            distanceText = "${detail.expected.expected_distance}km",
            estimatedTimeText = formatExpectedTimeInMinutes(detail.expected.expected_time),
            pickupDateText = formatPickupDateTime(
                detail.pickup.date,
                detail.pickup.time
            ),
            pickupAddressText = detail.depart.address,
            destinationAddressText = detail.dest.address,
            productDesc = detail.product.descript,
            productType = typeName,
            productWeight = weightName,
            productVolume = volumeName,
            requesterNickname = detail.requester_rating.nickname,
            requesterRatingText = "★ ${detail.requester_rating.star_rating} (${detail.requester_rating.deliveries})",
            departContactName = detail.depart_contact.name.orEmpty(),
            departContactPhone = detail.depart_contact.phone.orEmpty(),
            pictures = detail.product.pictures,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private fun getConfigProductType() = viewModelScope.launch {
        when (val res = configRepository.getProductTypeList()) {
            is ResultWrapper.Success -> productTypes.value = res.data
            else -> Unit
        }
    }

    private fun getConfigProductWeight() = viewModelScope.launch {
        when (val res = configRepository.getProductWeightList()) {
            is ResultWrapper.Success -> productWeights.value = res.data
            else -> Unit
        }
    }

    private fun getConfigProductVolume() = viewModelScope.launch {
        when (val res = configRepository.getProductVolumeList()) {
            is ResultWrapper.Success -> productVolumes.value = res.data
            else -> Unit
        }
    }

    private fun formatExpectedTimeInMinutes(expectedTimeSeconds: Int): String {
        val expectedMinutes = ceil(expectedTimeSeconds / 60.0).toInt().coerceAtLeast(1)
        return "${expectedMinutes}분"
    }

    private fun decideMapProvider() {
        val isKorea = Locale.getDefault().country.equals("KR", ignoreCase = true)
        mapShowState.value = if (isKorea) {
            HomeTabViewModel.MapShow.KAKAO_MAP
        } else {
            HomeTabViewModel.MapShow.GOOGLE_MAP
        }
    }

    private fun formatStatus(statusCd: String): String =
        when (statusCd) {
            "REGISTER_ING" -> "작성중"
            "MATCH_BEFORE" -> "매칭 대기중"
            "MATCH_ING" -> "매칭 진행중"
            "DELIVERY_BEFORE" -> "동행 대기중"
            "DELIVERY_START" -> "픽업 출발"
            "DELIVERY_ING" -> "동행중"
            "DELIVERY_END" -> "동행 완료"
            "CANCEL" -> "취소완료"
            else -> statusCd
        }

    enum class ButtonState(
        val primaryText: String,
        val secondaryText: String,
        val showSecondary: Boolean,
    ) {
        MatchBefore(primaryText = "지원하기", secondaryText = "채팅하기", showSecondary = true),
        PickupReady(primaryText = "픽업완료", secondaryText = "", showSecondary = false),
        DeliveryProgress(primaryText = "동행완료", secondaryText = "채팅하기", showSecondary = true),
        Done(primaryText = "등록하기", secondaryText = "", showSecondary = false),
        Hidden(primaryText = "", secondaryText = "", showSecondary = false),
    }

    fun confirmApply() {
        val detail = deliveryDetail.value ?: return
        requestApply(detail.delivery_id)
    }

    sealed class Event {
        object Back : Event()
        object Chat : Event()
        object DoneInfo : Event()
        object OpenReport : Event()
        object ShowApplyDialog : Event()
        object ActionFail : Event()
        data class ActionSuccess(val msg: String) : Event()
    }

    data class DeliverySummaryUiModel(
        val title: String,
        val statusText: String,
        val priceText: String,
        val pickupDateText: String,
        val distanceText: String,
        val estimatedTimeText: String,
        val pickupAddressText: String,
        val destinationAddressText: String,
        val productDesc: String,
        val productType: String,
        val productWeight: String,
        val productVolume: String,
        val requesterNickname: String,
        val requesterRatingText: String,
        val departContactName: String,
        val departContactPhone: String,
        val pictures: List<String>,
    )
}
