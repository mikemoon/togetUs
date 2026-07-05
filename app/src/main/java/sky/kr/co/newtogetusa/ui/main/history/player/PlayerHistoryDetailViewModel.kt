package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryDetailResponse
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.UserRepository
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
    private val userRepository: UserRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val deliveryDetail = MutableStateFlow<DeliveryDetailResponse?>(null)
    private val myUserId = MutableStateFlow<Long?>(null)
    private val myPlayerId = MutableStateFlow<Long?>(null)
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
        refreshMyProfile()
        when (val res = deliveryRepo.getDeliveryDetail(deliveryId)) {
            is ResultWrapper.Success -> deliveryDetail.value = res.data
            else -> {}
        }
    }

    private suspend fun refreshMyProfile() {
        val cached = dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)
        cached?.user?.let {
            myUserId.value = it.user_id.toLong()
            myPlayerId.value = it.player_id.takeIf { playerId -> playerId > 0 }?.toLong()
        }

        when (val res = userRepository.getMyProfile()) {
            is ResultWrapper.Success -> {
                dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, res.data)
                myUserId.value = res.data.user.user_id.toLong()
                myPlayerId.value = res.data.user.player_id.takeIf { it > 0 }?.toLong()
            }
            else -> Unit
        }
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    fun onSecondaryClick() {
        val detail = deliveryDetail.value ?: return
        when (buttonState.value) {
            ButtonState.PlayerSupport -> openChatRoom(detail)
            ButtonState.PlayerCancelSupport -> openChatRoom(detail)
            ButtonState.PlayerDeliveryProgress -> openChatRoom(detail)
            ButtonState.RequesterRegister -> _event.value = Event.CancelReq
            ButtonState.RequesterMatchBefore -> _event.value = Event.CancelReq
            else -> Unit
        }
    }

    fun onPrimaryClick() {
        val detail = deliveryDetail.value ?: return
        when (buttonState.value) {
            ButtonState.PlayerSupport -> _event.value = Event.ShowApplyDialog
            ButtonState.PlayerCancelSupport -> _event.value = Event.ConfirmCancelSupport
            ButtonState.PlayerPickupReady -> _event.value = Event.OpenProofPhoto(detail.delivery_id, "pickup")
            ButtonState.PlayerDeliveryProgress -> _event.value = Event.OpenProofPhoto(detail.delivery_id, "complete")
            ButtonState.PlayerDone -> _event.value = Event.OpenReport(isPlayer = true)
            ButtonState.RequesterRegister -> _event.value = Event.Modify
            ButtonState.RequesterMatchBefore -> _event.value = Event.ModifyFee
            ButtonState.RequesterCancel -> _event.value = Event.Modify
            else -> Unit
        }
    }

    fun onMarkClick() {
        val detail = deliveryDetail.value ?: return
        toggleLike(detail)
    }

    fun getMoreActions(): List<MoreAction> {
        val detail = deliveryDetail.value ?: return emptyList()
        val isMyPlayer = myPlayerId.value != null && detail.player_id == myPlayerId.value

        if (!isMyPlayer && detail.status_cd in listOf(
                "DELIVERY_BEFORE",
                "DELIVERY_WAIT",
                "DELIVERY_START",
                "PICKUP_START",
                "DELIVERY_DEPART",
                "DELIVERY_ING",
                "DELIVERY_END",
                "DONE",
                "DONE_END",
                "CANCEL"
            )
        ) {
            return emptyList()
        }

        return when (detail.status_cd) {
            "REGISTER_ING", "MATCH_BEFORE", "MATCH_ING" -> listOf(MoreAction.Report)
            "DELIVERY_BEFORE", "DELIVERY_WAIT" -> listOf(MoreAction.Cancel)
            "DELIVERY_START", "PICKUP_START", "DELIVERY_DEPART", "DELIVERY_ING" -> listOf(MoreAction.CustomerSupport)
            "DELIVERY_END", "DONE", "DONE_END" -> listOf(MoreAction.Chat, MoreAction.Settlement)
            else -> emptyList()
        }
    }

    fun onMoreActionClick(action: MoreAction) {
        val detail = deliveryDetail.value ?: return
        when (action) {
            MoreAction.Report -> _event.value = Event.OpenReportReason(detail.requester_id.toInt())
            MoreAction.Cancel -> _event.value = Event.CancelReq
            MoreAction.Chat -> openChatRoom(detail)
            MoreAction.CustomerSupport -> _event.value = Event.ActionSuccess("준비중입니다.")
            MoreAction.Settlement -> _event.value = Event.ActionSuccess("준비중입니다.")
        }
    }

    private fun requestAsk(deliveryId: Long) = viewModelScope.launch {
        /*when (deliveryRepo.putAsk(deliveryId)) {
            is ResultWrapper.Success -> _event.value = Event.ActionSuccess("문의가 전달되었어요.")
            else -> _event.value = Event.ActionFail
        }*/
    }

    private fun openChatRoom(detail: DeliveryDetailResponse) = viewModelScope.launch {
        when (val res = deliveryRepo.putPlayerChat(detail.delivery_id)) {
            is ResultWrapper.Success -> {
                if (res.data.room_id > 0L) {
                    _event.value = Event.OpenChatRoom(res.data.room_id)
                } else {
                    _event.value = Event.ActionFail
                }
            }
            else -> _event.value = Event.ActionFail
        }
    }

    private fun requestApply(deliveryId: Long) = viewModelScope.launch {
        when (val result = deliveryRepo.putApply(deliveryId)) {
            is ResultWrapper.Success -> if (result.data) {
                _event.value = Event.ActionSuccess("지원이 완료되었어요. 협의가 필요하시면 유저와의 채팅을 이용하세요.")
                deliveryDetail.value = deliveryDetail.value?.copy(is_apply = true)
                getDeliveryDetailInfo(deliveryId)
            } else _event.value = Event.ActionFail
            else -> _event.value = Event.ActionFail
        }
    }

    fun confirmCancelSupport() {
        val detail = deliveryDetail.value ?: return
        requestCancelSupport(detail.delivery_id)
    }

    private fun requestCancelSupport(deliveryId: Long) = viewModelScope.launch {
        when (val result = deliveryRepo.putApplyCancel(deliveryId)) {
            is ResultWrapper.Success -> if (result.data) {
                _event.value = Event.ActionSuccess("지원 취소가 완료되었습니다.")
                deliveryDetail.value = deliveryDetail.value?.copy(is_apply = false)
                getDeliveryDetailInfo(deliveryId)
            } else _event.value = Event.ActionFail
            else -> _event.value = Event.ActionFail
        }
    }

    private fun toggleLike(detail: DeliveryDetailResponse) = viewModelScope.launch {
        val nextLiked = !detail.is_like
        val result = if (detail.is_like) {
            deliveryRepo.postUnlikeDelivery(detail.delivery_id)
        } else {
            deliveryRepo.postLikeDelivery(detail.delivery_id)
        }

        when (result) {
            is ResultWrapper.Success -> {
                if (result.data) {
                    deliveryDetail.value = detail.copy(is_like = nextLiked)
                } else {
                    _event.value = Event.ActionFail
                }
            }
            else -> _event.value = Event.ActionFail
        }
    }

    fun reportRequester() = viewModelScope.launch {
        val detail = deliveryDetail.value ?: return@launch
        when (userRepository.postReportUser(
            detail.requester_id.toInt(),
            hashMapOf("report_cd" to "REPORT", "content" to "")
        )) {
            is ResultWrapper.Success -> _event.value = Event.ActionSuccess("신고가 접수되었습니다.")
            else -> _event.value = Event.ActionFail
        }
    }

    private fun requestPickupComplete(deliveryId: Long) = viewModelScope.launch {
        when (deliveryRepo.putPickupComplete(deliveryId)) {
            is ResultWrapper.Success -> {
                _event.value = Event.ActionSuccess("픽업 완료 처리되었어요.")
                getDeliveryDetailInfo(deliveryId)
            }
            else -> _event.value = Event.ActionFail
        }
    }

    fun cancelReq(deliveryId: Long, resultCallback: (Boolean) -> Unit) = viewModelScope.launch {
        when (deliveryRepo.cancelDelivery(deliveryId)) {
            is ResultWrapper.Success -> {
                resultCallback(true)
                getDeliveryDetailInfo(deliveryId)
            }
            else -> resultCallback(false)
        }
    }

    private fun requestDeliveryComplete(deliveryId: Long) = viewModelScope.launch {
        when (deliveryRepo.putDeliveryComplete(deliveryId)) {
            is ResultWrapper.Success -> {
                _event.value = Event.ActionSuccess("동행 완료 처리되었어요.")
                getDeliveryDetailInfo(deliveryId)
            }
            else -> _event.value = Event.ActionFail
        }
    }

    val buttonState = combine(deliveryDetail, myUserId, myPlayerId) { detail, userId, playerId ->
        if (detail == null) return@combine ButtonState.Hidden

        val isRequester = userId != null && detail.requester_id == userId
        val isAssignedPlayer = playerId != null && detail.player_id == playerId

        when {
            isRequester -> when (detail.status_cd) {
                "REGISTER_ING" -> ButtonState.RequesterRegister
                "MATCH_BEFORE", "MATCH_ING" -> ButtonState.RequesterMatchBefore
                "CANCEL" -> ButtonState.RequesterCancel
                "DONE", "DONE_END", "DELIVERY_END" -> ButtonState.RequesterDone
                else -> ButtonState.Hidden
            }
            isAssignedPlayer || detail.player_id == null || detail.status_cd in listOf("REGISTER_ING", "MATCH_BEFORE", "MATCH_ING") -> when (detail.status_cd) {
                "REGISTER_ING", "MATCH_BEFORE", "MATCH_ING" -> {
                    if (detail.is_apply) ButtonState.PlayerCancelSupport else ButtonState.PlayerSupport
                }
                "DELIVERY_BEFORE", "DELIVERY_WAIT", "DELIVERY_START", "PICKUP_START", "DELIVERY_DEPART" -> ButtonState.PlayerPickupReady
                "DELIVERY_ING" -> ButtonState.PlayerDeliveryProgress
                "DONE", "DONE_END", "DELIVERY_END" -> ButtonState.PlayerDone
                else -> ButtonState.Hidden
            }
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
            pickupAddressText = formatMaskedAddress(detail.depart.address, detail.depart.address2),
            destinationAddressText = formatMaskedAddress(detail.dest.address, detail.dest.address2),
            productDesc = detail.product.descript,
            productType = typeName,
            productWeight = weightName,
            productVolume = volumeName,
            requesterNickname = detail.requester_rating.nickname,
            requesterRatingText = "★ ${detail.requester_rating.star_rating} (${detail.requester_rating.deliveries})",
            departContactName = detail.depart_contact.name.orEmpty(),
            departContactPhone = detail.depart_contact.phone.orEmpty(),
            showDepartContact = hasContact(detail.depart_contact.name, detail.depart_contact.phone),
            destContactName = detail.dest_contact.name.orEmpty(),
            destContactPhone = detail.dest_contact.phone.orEmpty(),
            showDestContact = hasContact(detail.dest_contact.name, detail.dest_contact.phone),
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

    fun productTypeLabel(code: String): String =
        findConfigLabel(productTypes.value, code, PRODUCT_TYPE_FALLBACKS)

    fun productWeightLabel(code: String): String =
        findConfigLabel(productWeights.value, code, PRODUCT_WEIGHT_FALLBACKS)

    fun productVolumeLabel(code: String): String =
        findConfigLabel(productVolumes.value, code, PRODUCT_VOLUME_FALLBACKS)

    private fun formatExpectedTimeInMinutes(expectedTimeSeconds: Int): String {
        val expectedMinutes = ceil(expectedTimeSeconds / 60.0).toInt().coerceAtLeast(1)
        return "${expectedMinutes}분"
    }

    private fun formatMaskedAddress(address: String, address2: String?): String {
        val detail = address2?.trim().orEmpty()
        if (detail.isBlank() || detail.equals("null", ignoreCase = true) || detail == "-") {
            return address
        }

        val maskedDetail = detail.map { char ->
            if (char.isWhitespace()) char else '*'
        }.joinToString("")

        return "$address $maskedDetail"
    }

    private fun hasContact(name: String?, phone: String?): Boolean =
        listOf(name, phone).any { value ->
            val normalized = value?.trim().orEmpty()
            normalized.isNotBlank() &&
                !normalized.equals("null", ignoreCase = true) &&
                normalized != "-"
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
        PlayerSupport(primaryText = "지원하기", secondaryText = "채팅하기", showSecondary = true),
        PlayerCancelSupport(primaryText = "지원 취소", secondaryText = "채팅하기", showSecondary = true),
        PlayerPickupReady(primaryText = "픽업완료", secondaryText = "", showSecondary = false),
        PlayerDeliveryProgress(primaryText = "동행완료", secondaryText = "채팅하기", showSecondary = true),
        PlayerDone(primaryText = "등록하기", secondaryText = "", showSecondary = false),
        RequesterRegister(primaryText = "수정하기", secondaryText = "삭제하기", showSecondary = true),
        RequesterMatchBefore(primaryText = "추가금액수정", secondaryText = "취소하기", showSecondary = true),
        RequesterCancel(primaryText = "다시등록하기", secondaryText = "", showSecondary = false),
        RequesterDone(primaryText = "등록하기", secondaryText = "", showSecondary = false),
        Hidden(primaryText = "", secondaryText = "", showSecondary = false),
    }

    enum class MoreAction(val label: String) {
        Report("신고하기"),
        Cancel("취소하기"),
        Chat("채팅하기"),
        CustomerSupport("고객지원"),
        Settlement("정산내역"),
    }

    fun confirmApply() {
        val detail = deliveryDetail.value ?: return
        requestApply(detail.delivery_id)
    }

    sealed class Event {
        object Back : Event()
        object Chat : Event()
        object DoneInfo : Event()
        object CancelReq : Event()
        object Modify : Event()
        object ModifyFee : Event()
        object ConfirmReport : Event()
        object ConfirmCancelSupport : Event()
        data class OpenReport(val isPlayer: Boolean) : Event()
        data class OpenReportReason(val userId: Int) : Event()
        object ShowApplyDialog : Event()
        object ActionFail : Event()
        data class ActionSuccess(val msg: String) : Event()
        data class OpenChatRoom(val roomId: Long) : Event()
        data class OpenProofPhoto(val deliveryId: Long, val proofType: String) : Event()
    }

    companion object {
        private val PRODUCT_TYPE_FALLBACKS = mapOf(
            "type_1" to "전자기기"
        )
        private val PRODUCT_WEIGHT_FALLBACKS = mapOf(
            "small" to "가벼움 (~3KG)",
            "medium" to "가벼움 (~3KG)",
            "big" to "무거움 (3KG~)"
        )
        private val PRODUCT_VOLUME_FALLBACKS = mapOf(
            "small" to "작음 (작은 상자/에코백 수준)",
            "medium" to "보통",
            "big" to "큼"
        )

        private fun findConfigLabel(
            configs: List<BaseCommonDto>,
            code: String,
            fallback: Map<String, String>
        ): String =
            configs.firstOrNull { it.code == code }?.name?.takeIf { it.isNotBlank() }
                ?: fallback[code].orEmpty()
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
        val showDepartContact: Boolean,
        val destContactName: String,
        val destContactPhone: String,
        val showDestContact: Boolean,
        val pictures: List<String>,
    )
}
