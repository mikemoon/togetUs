package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryFeeResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.PickupDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PortOneConfigDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryPayPickupRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryPayRequest
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.utils.TextConvertUtil.toWon
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DeliveryPayViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val deliveryRepository: DeliveryRepository,
    private val configRepository: ConfigRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val feeText = MutableLiveData("-")
    val adjustFeeText = MutableLiveData("0원")
    val totalFeeText = MutableLiveData("-")
    val distanceText = MutableLiveData("-")
    val weightText = MutableLiveData("-")
    val pickupDateText = MutableLiveData("픽업일")
    val pickupTimeText = MutableLiveData("픽업 시간")
    val pickupDateEnabled = MutableLiveData(false)
    val pickupTimeEnabled = MutableLiveData(false)

    val agreeTerm1 = MutableLiveData(false)
    val agreeTerm2 = MutableLiveData(false)
    val agreeTerm3 = MutableLiveData(false)
    val policyAgree = MutableLiveData(false)
    val payEnabled = MutableLiveData(false)

    private var deliveryId: Long = -1L
    private var playerId: Long = -1L
    private var fee: DeliveryFeeResponse? = null
    private var adjustFee: Long = 0L
    private var pickupDateForApi: String? = null
    private var pickupTimeForApi: String? = null
    private var needPickupDate = false
    private var needPickupTime = false
    private val termsCodes = listOf("term_1", "term_2", "term_3")
    private val apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val rawDateFormatter = DateTimeFormatter.BASIC_ISO_DATE
    private val apiTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val uiDateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
    private val uiTimeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun load(deliveryId: Long, playerId: Long) {
        if (deliveryId <= 0L || this.deliveryId == deliveryId) return
        this.deliveryId = deliveryId
        this.playerId = playerId
        loadWeightNames()
        viewModelScope.launch {
            loadingState.value = true
            when (val res = deliveryRepository.getDeliveryFee(deliveryId)) {
                is ResultWrapper.Success -> bindFee(res.data)
                is ResultWrapper.GenericError -> _event.value = Event.ShowMessage(res.message ?: "요금 정보를 불러오지 못했습니다.")
                is ResultWrapper.NetworkError -> _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
            when (val detail = deliveryRepository.getDeliveryDetail(deliveryId)) {
                is ResultWrapper.Success -> bindPickup(detail.data.pickup)
                is ResultWrapper.GenericError -> _event.value = Event.ShowMessage(detail.message ?: "픽업 정보를 불러오지 못했습니다.")
                is ResultWrapper.NetworkError -> _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
            loadingState.value = false
        }
    }

    fun onAdjustFeeChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        adjustFee = text.toString().replace(",", "").trim().toLongOrNull() ?: 0L
        refreshFeeTexts()
    }

    fun toggleTerm(index: Int) {
        when (index) {
            1 -> agreeTerm1.value = agreeTerm1.value != true
            2 -> agreeTerm2.value = agreeTerm2.value != true
            3 -> agreeTerm3.value = agreeTerm3.value != true
        }
        updatePayEnabled()
    }

    // 하단 정책 동의 체크 (iOS policyCheck 대응)
    fun togglePolicy() {
        policyAgree.value = policyAgree.value != true
        updatePayEnabled()
    }

    fun onPayClick() {
        if (deliveryId <= 0L) {
            _event.value = Event.ShowMessage("배송 정보를 확인할 수 없습니다.")
            return
        }
        if (playerId <= 0L) {
            _event.value = Event.ShowMessage("플레이어 정보를 확인할 수 없습니다.")
            return
        }
        if (payEnabled.value != true) return
        if (adjustFee > 0L && (adjustFee < 1000L || adjustFee > 100000L || adjustFee % 1000L != 0L)) {
            _event.value = Event.ShowMessage("천원 단위로만 입력 가능해요.")
            return
        }
        if (!isPickupComplete()) {
            _event.value = Event.ShowMessage("픽업일과 시간을 선택해 주세요.")
            return
        }

        // 결제 동의 팝업 → 모두 동의 시 결제 진행 (iOS presentAgreePopup 대응)
        _event.value = Event.ShowAgreePopup
    }

    // 결제 동의 팝업에서 모두 동의 시 호출
    fun onAgreeAll() {
        if (deliveryId <= 0L || playerId <= 0L) return
        viewModelScope.launch {
            loadingState.value = true
            when (val config = deliveryRepository.getDeliveryPortOneConfig()) {
                is ResultWrapper.Success -> _event.value = Event.StartPortOnePayment(config.data, totalAmount())
                is ResultWrapper.GenericError -> _event.value = Event.ShowMessage(config.message ?: "결제 설정을 불러오지 못했습니다.")
                is ResultWrapper.NetworkError -> _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
            }
            loadingState.value = false
        }
    }

    fun confirmPayment(paymentId: String) = viewModelScope.launch {
        if (deliveryId <= 0L || playerId <= 0L) return@launch
        val date = pickupDateForApi
        val time = pickupTimeForApi
        if (date.isNullOrBlank() || time.isNullOrBlank()) {
            _event.value = Event.ShowMessage("픽업일과 시간을 선택해 주세요.")
            return@launch
        }
        loadingState.value = true
        val request = DeliveryPayRequest(
            playerId = playerId,
            feeAdjust = adjustFee,
            feeFinal = totalAmount(),
            paymentId = paymentId,
            pickup = DeliveryPayPickupRequest(date = date, time = time),
            termsCodes = termsCodes
        )
        when (val res = deliveryRepository.payDelivery(deliveryId, request)) {
            is ResultWrapper.Success -> _event.value = Event.PaymentSuccess
            is ResultWrapper.GenericError -> _event.value = Event.ShowMessage(res.message ?: "결제 처리에 실패했습니다.")
            is ResultWrapper.NetworkError -> _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
        }
        loadingState.value = false
    }

    fun onEventClick(event: Event) {
        _event.value = event
    }

    private fun bindFee(data: DeliveryFeeResponse) {
        fee = data
        distanceText.value = "${data.expectedStraight}km"
        weightText.value = resolveWeightName(data.expectedWeightCd)
        refreshFeeTexts()
    }

    // 무게 코드 → UI 명칭 (iOS getDeliveryProductWeightName 대응)
    private var weightCodeNameMap: Map<String, String> = emptyMap()

    private fun loadWeightNames() = viewModelScope.launch {
        when (val res = configRepository.getProductWeightList()) {
            is ResultWrapper.Success -> {
                weightCodeNameMap = res.data.associate { it.code to it.name }
                fee?.let { weightText.value = resolveWeightName(it.expectedWeightCd) }
            }
            else -> {}
        }
    }

    private fun resolveWeightName(code: String): String {
        val configName = weightCodeNameMap[code]
        if (!configName.isNullOrBlank() && !configName.equals(code, ignoreCase = true)) {
            return configName
        }
        return when (code.trim().lowercase()) {
            "light", "small" -> "가벼움(~3KG)"
            "medium" -> "보통(3~10KG)"
            "heavy", "big" -> "무거움(10KG~)"
            else -> "-"
        }
    }

    private fun bindPickup(pickup: PickupDto) {
        val date = pickup.date.toLocalDateOrNull()
        val time = pickup.time.toLocalTimeOrNull()
        val isNegotiableTime = !pickup.is_immediately && (pickup.time.isBlank() || pickup.time == "0000")

        needPickupDate = pickup.is_immediately
        needPickupTime = pickup.is_immediately || isNegotiableTime
        pickupDateEnabled.value = pickup.is_immediately
        pickupTimeEnabled.value = pickup.is_immediately || isNegotiableTime

        if (pickup.is_immediately) {
            pickupDateForApi = null
            pickupTimeForApi = null
            pickupDateText.value = "픽업일"
            pickupTimeText.value = "픽업 시간"
        } else {
            pickupDateForApi = date?.format(apiDateFormatter)
            pickupDateText.value = date?.format(uiDateFormatter) ?: "픽업일"
            if (isNegotiableTime) {
                pickupTimeForApi = null
                pickupTimeText.value = "픽업 시간"
            } else {
                pickupTimeForApi = time?.format(apiTimeFormatter)
                pickupTimeText.value = time?.format(uiTimeFormatter) ?: "픽업 시간"
            }
        }
        updatePayEnabled()
    }

    fun setPickupDate(date: LocalDate) {
        pickupDateForApi = date.format(apiDateFormatter)
        pickupDateText.value = date.format(uiDateFormatter)
        updatePayEnabled()
    }

    fun setPickupTime(hour: Int, minute: Int) {
        if (hour < 0 || minute < 0) return
        val time = LocalTime.of(hour, minute)
        pickupTimeForApi = time.format(apiTimeFormatter)
        pickupTimeText.value = time.format(uiTimeFormatter)
        updatePayEnabled()
    }

    private fun refreshFeeTexts() {
        val base = fee?.feeBasic ?: 0
        feeText.value = base.toWon()
        adjustFeeText.value = adjustFee.toInt().toWon()
        totalFeeText.value = totalAmount().toInt().toWon()
        updatePayEnabled()
    }

    private fun totalAmount(): Long {
        val base = fee?.feeBasic?.toLong() ?: 0L
        return base + adjustFee
    }

    private fun updatePayEnabled() {
        // iOS: 빈 값(추가요금 0) 허용, 아니면 1,000원 단위 (최소 1,000 ~ 최대 100,000)
        val feeValid = adjustFee == 0L ||
            (adjustFee in 1000L..100000L && adjustFee % 1000L == 0L)
        payEnabled.value = policyAgree.value == true && feeValid && totalAmount() > 0L && isPickupComplete()
    }

    private fun isPickupComplete(): Boolean {
        val dateOk = !needPickupDate || !pickupDateForApi.isNullOrBlank()
        val timeOk = !needPickupTime || !pickupTimeForApi.isNullOrBlank()
        return dateOk && timeOk
    }

    fun selectedPickupDate(): LocalDate? = pickupDateForApi?.let {
        runCatching { LocalDate.parse(it, apiDateFormatter) }.getOrNull()
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        val clean = trim()
        return runCatching {
            when {
                clean.length == 8 -> LocalDate.parse(clean, rawDateFormatter)
                clean.length >= 10 -> LocalDate.parse(clean.take(10), apiDateFormatter)
                else -> null
            }
        }.getOrNull()
    }

    private fun String.toLocalTimeOrNull(): LocalTime? {
        val clean = trim()
        return runCatching {
            when {
                clean.length == 4 -> LocalTime.of(clean.take(2).toInt(), clean.takeLast(2).toInt())
                clean.length >= 5 -> LocalTime.parse(clean.take(5), apiTimeFormatter)
                else -> null
            }
        }.getOrNull()
    }

    sealed class Event {
        object Back : Event()
        object SelectPickupDate : Event()
        object SelectPickupTime : Event()
        object ShowAgreePopup : Event()
        data class StartPortOnePayment(val config: PortOneConfigDto, val amount: Long) : Event()
        object PaymentSuccess : Event()
        data class ShowMessage(val message: String) : Event()
    }
}
