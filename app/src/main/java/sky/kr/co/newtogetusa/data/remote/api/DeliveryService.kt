package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryFeeResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryItemDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryResponse
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest

interface DeliveryService {

    @PUT("api/deliveries/v1/{delivery_id}/pickup") //플레이어 픽업완료
    suspend fun putPickupComplete(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @PUT("api/deliveries/v1/{delivery_id}/complete")//플레이어 배송완료
    suspend fun putDeliveryComplete(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @PUT("api/deliveries/v1/{delivery_id}/ask") //플레이어가 배송요청에 대해 문의
    suspend fun putAsk(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @PUT("api/deliveries/v1/{delivery_id}/apply") //플레이어가 거래요청
    suspend fun putApply(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @PUT("api/deliveries/v1/{delivery_id}/suggest") //플레이어에게 배송요청 제안
    suspend fun putSuggest(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @PUT("api/deliveries/v1/{delivery_id}/accept") //플레이어에게 배송요청 확정
    suspend fun putAccept(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @GET("api/deliveries/v1/{delivery_id}/players") //배송요청에 관련된 플레이어 목록
    suspend fun getPlayers(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @POST("api/deliveries/v1/{delivery_id}/review")//배송완료후 리뷰
    suspend fun postReview(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @GET("api/deliveries/v1/{delivery_id}/review/check") //리뷰등록전 유효성체크
    suspend fun getReviewCheck(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @PUT("api/deliveries/v1/{delivery_id}/fee")//배송요청 요금확정
    suspend fun putFee(
        @Path("delivery_id") delivery_id: Int,
    ):Boolean

    @POST("api/deliverys/v1") //배송요청 등록
    suspend fun postDelivery(
        @Body body: DeliveryRequest
    ): DeliveryResponse

    @GET("api/deliverys/v1") //배송요청 목록 보기
    suspend fun getDeliveryList(
    ):List<DeliveryItemDto>

    @GET("api/deliveries/v1/{delivery_id}")
    suspend fun getDeliveryDetail(
        @Path("delivery_id") delivery_id: Long,
    ): Boolean

    @GET("api/deliverys/v1/{delivery_id}/fee") //요금확인
    suspend fun getDeliveryFee(
        @Path("delivery_id") delivery_id: Long,
    ): DeliveryFeeResponse

    @PUT("api/deliverys/v1/{delivery_id}/fee")
    suspend fun putDeliveryFinalReq(
        @Path("delivery_id") delivery_id: Long,
        @Body body: DeliveryFinalReq,
    ): Boolean


}