package sky.kr.co.newtogetusa.ui.main.history

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
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel.MapShow
import sky.kr.co.newtogetusa.utils.DeliveryStatusBadgeUtil
import sky.kr.co.newtogetusa.utils.TextConvertUtil.formatPickupDateTime
import sky.kr.co.newtogetusa.utils.TextConvertUtil.formatWon
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                 private val deliveryRepo : DeliveryRepository,
                                                 private val configRepository: ConfigRepository,
                                                 private val userRepository: UserRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){

    val deliveryDetail = MutableStateFlow<DeliveryDetailResponse?>(null)
    private val myUserId = MutableStateFlow<Long?>(null)
    private val chatInProgressCount = MutableStateFlow(0)
    private val detailState = combine(
        deliveryDetail,
        myUserId,
        chatInProgressCount
    ) { detail, userId, chatCount ->
        DetailState(detail, userId, chatCount)
    }

    val productTypes = MutableStateFlow<List<BaseCommonDto>>(emptyList())
    val productWeights = MutableStateFlow<List<BaseCommonDto>>(emptyList())
    val productVolumes = MutableStateFlow<List<BaseCommonDto>>(emptyList())

    init {
        getConfigProductType()
        getConfigProductWeight()
        getConfigProductVolume()
    }

    fun getConfigProductType() = viewModelScope.launch {
        val res = configRepository.getProductTypeList()
        when(res){
            is ResultWrapper.Success -> {
                productTypes.value = res.data
            }
            else -> {}
        }
    }

    fun getConfigProductWeight() = viewModelScope.launch {
        val res = configRepository.getProductWeightList()
        when(res){
            is ResultWrapper.Success -> {
                productWeights.value = res.data
            }
            else -> {}
        }
    }

    fun getConfigProductVolume() = viewModelScope.launch {
        val res = configRepository.getProductVolumeList()
        when(res){
            is ResultWrapper.Success -> {
                productVolumes.value = res.data
            }
            else -> {}
        }
    }

    fun productTypeLabel(code: String): String =
        findConfigLabel(productTypes.value, code, PRODUCT_TYPE_FALLBACKS)

    fun productWeightLabel(code: String): String =
        findConfigLabel(productWeights.value, code, PRODUCT_WEIGHT_FALLBACKS)

    fun productVolumeLabel(code: String): String =
        findConfigLabel(productVolumes.value, code, PRODUCT_VOLUME_FALLBACKS)

    fun getDeliveryDetailInfo(deliveryId: Long) = viewModelScope.launch {
        refreshMyProfile()
        val res = deliveryRepo.getDeliveryDetail(deliveryId)
        when(res){
            is ResultWrapper.Success ->{
                deliveryDetail.value = res.data
                refreshChatInProgressCountIfNeeded(res.data)
            }
            else -> {}
        }
    }

    private suspend fun refreshMyProfile() {
        dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)?.user?.let {
            myUserId.value = it.user_id.toLong()
        }

        when (val res = userRepository.getMyProfile()) {
            is ResultWrapper.Success -> {
                dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, res.data)
                myUserId.value = res.data.user.user_id.toLong()
            }
            else -> Unit
        }
    }

    private suspend fun refreshChatInProgressCountIfNeeded(detail: DeliveryDetailResponse) {
        val isRequester = myUserId.value != null && detail.requester_id == myUserId.value
        val shouldShow = isRequester && detail.status_cd in setOf("MATCH_BEFORE", "MATCH_ING")
        if (!shouldShow) {
            chatInProgressCount.value = 0
            return
        }

        when (val res = deliveryRepo.getChatInProgressList(detail.delivery_id)) {
            is ResultWrapper.Success -> chatInProgressCount.value = res.data.size
            else -> chatInProgressCount.value = 0
        }
    }

    fun cancelReq(deliveryId: Long, resultCallback: (Boolean) -> Unit) = viewModelScope.launch {
        val res = deliveryRepo.cancelDelivery(deliveryId)
        when(res){
            is ResultWrapper.Success ->{
                resultCallback.invoke(res.data)
            }
            else -> {
                resultCallback.invoke(false)
            }
        }
    }

    val deliveryData = combine(
        detailState,
        productTypes,
        productWeights,
        productVolumes
    ) { state, types, weights, volumes ->

        val detail = state.detail ?: return@combine null

        val typeName = types.firstOrNull { it.code == detail.product.type_cd }?.name.orEmpty()
        val weightName = weights.firstOrNull { it.code == detail.product.weight_cd }?.name.orEmpty()
        val volumeName = volumes.firstOrNull { it.code == detail.product.volume_cd }?.name.orEmpty()
        val isRequester = state.userId != null && detail.requester_id == state.userId
        val showPaymentInfo = isRequester &&
            detail.status_cd in setOf("DELIVERY_BEFORE", "DELIVERY_WAIT", "DELIVERY_START", "DELIVERY_ING", "DELIVERY_END", "DONE", "DONE_END", "CANCEL") &&
            detail.pay.isNotEmpty()
        val primaryPay = detail.pay.firstOrNull()
        val additionalPay = detail.pay.drop(1).firstOrNull()

        DeliverySummaryUiModel(
            statusCode = detail.status_cd,
            statusText = DeliveryStatusBadgeUtil.titleOf(detail.status_cd),
            title = detail.title,
            priceText = formatWon(detail.fee.fee_final),
            distanceText = "${detail.expected.expected_distance}km",
            estimatedTimeText = formatExpectedTimeInMinutes(detail.expected.expected_time),
            pickupDateText = formatPickupDateTime(
                detail.pickup.date,
                detail.pickup.time
            ),
            pickupAddressText = detail.depart.address,
            destinationAddressText = detail.dest.address,
            departContactName = detail.depart_contact.name.orEmpty(),
            departContactPhone = detail.depart_contact.phone.orEmpty(),
            destContactName = detail.dest_contact.name.orEmpty(),
            destContactPhone = detail.dest_contact.phone.orEmpty(),
            productPicUrls = detail.product.pictures,
            productDesc = detail.product.descript,
            productType = typeName,
            productWeight = weightName,
            productVolume = volumeName,
            showCompanionInfo = isRequester && detail.status_cd in setOf("MATCH_BEFORE", "MATCH_ING"),
            hasChatInProgress = state.chatCount > 0,
            chatInProgressText = "진행중 채팅",
            chatInProgressCountText = state.chatCount.toString(),
            showChatEmptyText = state.chatCount == 0,
            showPaymentInfo = showPaymentInfo,
            basePaymentText = formatWon(primaryPay?.amount ?: detail.fee.fee_final),
            basicFeeText = formatWon(detail.fee.fee_basic),
            adjustFeeText = formatWon(detail.fee.fee_adjust),
            paymentMethodText = formatPayMethod(primaryPay?.type),
            showAdditionalPaymentInfo = showPaymentInfo && additionalPay?.amount != null,
            additionalPaymentText = formatWon(additionalPay?.amount ?: 0),
            additionalPaymentMethodText = formatPayMethod(additionalPay?.type),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val mapShowState = MutableStateFlow<MapShow>(MapShow.LOCAL_IMAGE)

    init {
        decideMapProvider()
    }

    private fun decideMapProvider() {
        val isKorea = Locale.getDefault().country.equals("KR", ignoreCase = true)

        mapShowState.value = if (isKorea) {
            MapShow.KAKAO_MAP
        } else {
            MapShow.GOOGLE_MAP
        }
    }

    private fun formatExpectedTimeInMinutes(expectedTimeSeconds: Int): String {
        val expectedMinutes = ceil(expectedTimeSeconds / 60.0).toInt().coerceAtLeast(1)
        return "${expectedMinutes}분"
    }

    private fun formatPayMethod(type: String?): String =
        when (type?.uppercase(Locale.getDefault())) {
            "CARD", "CREDIT_CARD", "CREDITCARD" -> "신용카드"
            "KAKAO_PAY", "KAKAOPAY" -> "카카오페이"
            "NAVER_PAY", "NAVERPAY" -> "네이버페이"
            "BANK", "TRANSFER", "VACCOUNT" -> "계좌이체"
            null, "" -> "-"
            else -> type
        }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event{
        object Back : Event()

        object MoreShow : Event()
        object CancelReq: Event()
        object Modify: Event()

        object ModifyFee: Event()

        object ChatInProgress: Event()


    }

    private fun findNameByCode(
        list: List<BaseCommonDto>,
        code: String
    ): String {
        return list.firstOrNull { it.code == code }?.name.orEmpty()
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

    private data class DetailState(
        val detail: DeliveryDetailResponse?,
        val userId: Long?,
        val chatCount: Int,
    )

    data class DeliverySummaryUiModel(
        val statusCode: String,
        val statusText: String,
        val title: String,
        val priceText : String,
        val pickupDateText : String,
        val distanceText : String,
        val estimatedTimeText : String,
        val pickupAddressText : String,
        val destinationAddressText : String,
        val departContactName : String,
        val departContactPhone: String,
        val destContactName : String,
        val destContactPhone: String,
        val productPicUrls : List<String>,
        val productDesc : String,
        val productType : String,
        val productWeight : String,
        val productVolume : String,
        val showCompanionInfo: Boolean,
        val hasChatInProgress: Boolean,
        val chatInProgressText: String,
        val chatInProgressCountText: String,
        val showChatEmptyText: Boolean,
        val showPaymentInfo: Boolean,
        val basePaymentText: String,
        val basicFeeText: String,
        val adjustFeeText: String,
        val paymentMethodText: String,
        val showAdditionalPaymentInfo: Boolean,
        val additionalPaymentText: String,
        val additionalPaymentMethodText: String,
    )
}
