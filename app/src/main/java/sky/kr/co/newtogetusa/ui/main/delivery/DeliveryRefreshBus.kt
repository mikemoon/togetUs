package sky.kr.co.newtogetusa.ui.main.delivery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * iOS PushManager의 refreshDeliveryInfo / refreshDeliveryList 대응.
 * 포그라운드에서 동행(배송) 푸시 수신 시 상세/목록 화면 갱신용.
 */
object DeliveryRefreshBus {

    private val _detailEvents = MutableSharedFlow<Long>(extraBufferCapacity = 8)
    val detailEvents = _detailEvents.asSharedFlow()

    private val _listEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val listEvents = _listEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastListRefreshAt = 0L
    private var listRefreshJob: Job? = null

    fun notifyDeliveryUpdated(deliveryId: Long) {
        _detailEvents.tryEmit(deliveryId)
        scheduleListRefresh()
    }

    // 목록 갱신은 1초 간격으로 묶어서 발송 (첫 건 즉시, 1초 내 몰린 건 마지막 1회만)
    private fun scheduleListRefresh() {
        listRefreshJob?.cancel()
        val elapsed = System.currentTimeMillis() - lastListRefreshAt
        if (elapsed >= LIST_REFRESH_INTERVAL_MS) {
            lastListRefreshAt = System.currentTimeMillis()
            _listEvents.tryEmit(Unit)
        } else {
            listRefreshJob = scope.launch {
                delay(LIST_REFRESH_INTERVAL_MS - elapsed)
                lastListRefreshAt = System.currentTimeMillis()
                _listEvents.tryEmit(Unit)
            }
        }
    }

    private const val LIST_REFRESH_INTERVAL_MS = 1000L
}
