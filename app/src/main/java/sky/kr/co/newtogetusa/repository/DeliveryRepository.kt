package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.DeliveryService
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryFeeResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryItemDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryResponse
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class DeliveryRepository @Inject constructor(
    @NetworkModule.DeliveryApi private val apiService: DeliveryService
) : BaseNetRepo() {

    suspend fun getDeliveryList() = safeApiCall<List<DeliveryItemDto>>(Dispatchers.IO){
        apiService.getDeliveryList()
    }

    suspend fun postDelivery(req: DeliveryRequest) = safeApiCall<DeliveryResponse>(Dispatchers.IO){
        apiService.postDelivery(req)
    }

    suspend fun getDeliveryDetail(deliveryId: Long) = safeApiCall<Boolean>(Dispatchers.IO){
        apiService.getDeliveryDetail(deliveryId)
    }

    suspend fun getDeliveryFee(deliveryId: Long) = safeApiCall<DeliveryFeeResponse>(Dispatchers.IO){
        apiService.getDeliveryFee(deliveryId)
    }

    suspend fun putDeliveryFinalReq(deliveryId: Long, deliveryFinalReq: DeliveryFinalReq) = safeApiCall<Boolean>(
        Dispatchers.IO){
        apiService.putDeliveryFinalReq(deliveryId, deliveryFinalReq)
    }
}