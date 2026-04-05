package sky.kr.co.newtogetusa.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.DeliveryService
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryDetailResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryFeeResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryItemDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySearchResponse
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRegPhoto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryUploadPictureRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import sky.kr.co.newtogetusa.repository.page.DeliveryPagingSource
import sky.kr.co.newtogetusa.repository.page.DeliveryPlayerPagingSource
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

    suspend fun getDeliveryDetail(deliveryId: Long) = safeApiCall<DeliveryDetailResponse>(Dispatchers.IO){
        apiService.getDeliveryDetail(deliveryId)
    }

    suspend fun getDeliveryFee(deliveryId: Long) = safeApiCall<DeliveryFeeResponse>(Dispatchers.IO){
        apiService.getDeliveryFee(deliveryId)
    }

    suspend fun putDeliveryFinalReq(deliveryId: Long, deliveryFinalReq: DeliveryFinalReq) = safeApiCall<Boolean>(
        Dispatchers.IO){
        apiService.putDeliveryFinalReq(deliveryId, deliveryFinalReq)
    }

    suspend fun postDeliverySearch(deliverySearchReq: DeliverySearchReq) = safeApiCall<DeliverySearchResponse>(Dispatchers.IO){
        apiService.postDeliverySearch(deliverySearchReq)
    }

    suspend fun postDeliveryPicture(deliveryId: Long, deliveryRegPhoto: DeliveryRegPhoto) = safeApiCall<Boolean>(
        Dispatchers.IO){
        apiService.postDeliveryPicture(deliveryId,deliveryRegPhoto)
    }

    suspend fun postDeliveryPictures(deliveryId: Long, photoList: List<DeliveryRegPhoto>) = safeApiCall<Boolean>(
        Dispatchers.IO){
        apiService.postDeliveryPictures(deliveryId, photoList)
    }

    suspend fun putDeliveryCompletePicture(deliveryId: Long, body: DeliveryUploadPictureRequest) = safeApiCall<Boolean>(
        Dispatchers.IO) {
        apiService.putDeliveryCompletePicture(deliveryId, body)
    }

    suspend fun cancelDelivery(deliveryId: Long) = safeApiCall<Boolean>(Dispatchers.IO){
        apiService.cancelDelivery(deliveryId)
    }

    suspend fun putApply(deliveryId: Long) = safeApiCall(Dispatchers.IO){
        apiService.putApply(deliveryId)
    }

    fun getDeliveryPagingFlow(type: String, title: String) =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                DeliveryPagingSource(this, type, title)
            }
        ).flow

    suspend fun putSuggest(deliveryId: Long, body: Map<String, String>) = safeApiCall<Boolean>(Dispatchers.IO) {
        apiService.putSuggest(deliveryId, body)
    }

    //플레이어
    suspend fun postPlayerDeliverySearch(deliverySearchReq: DeliverySearchReq) = safeApiCall<DeliverySearchResponse>(Dispatchers.IO){
        apiService.postPlayerDeliverySearch(deliverySearchReq)
    }

    fun getPlayerDeliveryPagingFlow(type: String, title: String) =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                DeliveryPlayerPagingSource(this, type, title)
            }
        ).flow

}