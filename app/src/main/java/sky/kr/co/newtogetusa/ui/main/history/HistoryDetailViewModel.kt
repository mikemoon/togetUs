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
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel.MapShow
import sky.kr.co.newtogetusa.utils.TextConvertUtil.formatPickupDateTime
import sky.kr.co.newtogetusa.utils.TextConvertUtil.formatWon
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                 private val deliveryRepo : DeliveryRepository,
                                                 private val configRepository: ConfigRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){

    val deliveryDetail = MutableStateFlow<DeliveryDetailResponse?>(null)

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

    fun getDeliveryDetailInfo(deliveryId: Long) = viewModelScope.launch {
        val res = deliveryRepo.getDeliveryDetail(deliveryId)
        when(res){
            is ResultWrapper.Success ->{
                deliveryDetail.value = res.data
            }
            else -> {}
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
        deliveryDetail,
        productTypes,
        productWeights,
        productVolumes
    ) { detail, types, weights, volumes ->

        if (detail == null) return@combine null

        val typeName = types.firstOrNull { it.code == detail.product.type_cd }?.name.orEmpty()
        val weightName = weights.firstOrNull { it.code == detail.product.weight_cd }?.name.orEmpty()
        val volumeName = volumes.firstOrNull { it.code == detail.product.volume_cd }?.name.orEmpty()

        DeliverySummaryUiModel(
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
            productVolume = volumeName
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


    }

    private fun findNameByCode(
        list: List<BaseCommonDto>,
        code: String
    ): String {
        return list.firstOrNull { it.code == code }?.name.orEmpty()
    }

    data class DeliverySummaryUiModel(
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
    )
}