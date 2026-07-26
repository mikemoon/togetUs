package sky.kr.co.newtogetusa.data.remote.api

import com.squareup.okhttp.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ChatInProgressDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryDetailResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryFeeResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryItemDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryPlayerChatDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryReviewDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.PlayerGpsResponseDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySearchResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryStatusLogDto
import sky.kr.co.newtogetusa.data.remote.dto.delivery.ReviewCheckDto
import sky.kr.co.newtogetusa.data.remote.dto.CommonBoolDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PortOneConfigDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryPayRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRegPhoto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryReviewRequest
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryUploadPictureRequest

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

    @PUT("/api/deliverys/v1/{delivery_id}/requester/suggest") //플레이어에게 배송요청 제안
    suspend fun putSuggest(
        @Path("delivery_id") delivery_id: Long,
        @Body body : Map<String, String>
    ):Boolean

    @PUT("api/deliverys/v1/{delivery_id}/requester/accept") //플레이어 선택
    suspend fun putAccept(
        @Path("delivery_id") delivery_id: Int,
        @Body body: Map<String, Long>
    ): CommonBoolDto

    @PUT("api/deliverys/v1/{delivery_id}/requester/apply_reject") //지원 거절
    suspend fun rejectApply(
        @Path("delivery_id") deliveryId: Long,
        @Body body: Map<String, Long>
    ): CommonBoolDto

    @PUT("api/deliverys/v1/{delivery_id}/requester/suggest_cancel") //제안 취소
    suspend fun cancelSuggest(
        @Path("delivery_id") deliveryId: Long,
        @Body body: Map<String, Long>
    ): CommonBoolDto

    @GET("api/deliverys/v1/portone/config")
    suspend fun getDeliveryPortOneConfig(): PortOneConfigDto

    @POST("api/deliverys/v1/{delivery_id}/pay")
    suspend fun payDelivery(
        @Path("delivery_id") deliveryId: Long,
        @Body body: DeliveryPayRequest
    ): CommonBoolDto

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

    @POST("api/deliverys/v1/{delivery_id}") //배송요청 수정
    suspend fun editDelivery(
        @Path("delivery_id") delivery_id: Long,
        @Body body: DeliveryRequest
    ): Boolean

    @GET("api/deliverys/v1") //배송요청 목록 보기
    suspend fun getDeliveryList(
    ):List<DeliveryItemDto>

    @GET("api/deliverys/v1/{delivery_id}")
    suspend fun getDeliveryDetail(
        @Path("delivery_id") delivery_id: Long,
    ): DeliveryDetailResponse

    @GET("api/deliverys/v1/{delivery_id}/gps") //플레이어 GPS 조회
    suspend fun getPlayerGps(
        @Path("delivery_id") delivery_id: Long,
    ): PlayerGpsResponseDto

    @GET("api/deliverys/v1/{delivery_id}/fee") //요금확인
    suspend fun getDeliveryFee(
        @Path("delivery_id") delivery_id: Long,
    ): DeliveryFeeResponse

    @PUT("api/deliverys/v1/{delivery_id}/fee") //배송등록
    suspend fun putDeliveryFinalReq(
        @Path("delivery_id") delivery_id: Long,
        @Body body: DeliveryFinalReq,
    ): Boolean

    @GET("api/deliverys/v1/{delivery_id}/requester/status_log") //현황조회
    suspend fun getDeliveryStatusList(
        @Path("delivery_id") delivery_id: Long,
    ): List<DeliveryStatusLogDto>

    @PUT("api/deliverys/v1/{delivery_id}/requester/pickup")
    suspend fun putRequesterPickup(
        @Path("delivery_id") deliveryId: Long,
    ): Boolean

    @GET("api/deliverys/v1/{delivery_id}/requester/chatrooms")
    suspend fun getChatInProgressList(
        @Path("delivery_id") deliveryId: Long,
    ): List<ChatInProgressDto>

    @GET("api/deliverys/v1/{delivery_id}/requester/review/check")
    suspend fun checkReviewRequester(
        @Path("delivery_id") deliveryId: Long,
    ): ReviewCheckDto

    @POST("api/deliverys/v1/{delivery_id}/requester/review")
    suspend fun writeReviewRequester(
        @Path("delivery_id") deliveryId: Long,
        @Body body: DeliveryReviewRequest,
    ): Boolean

    @GET("api/deliverys/v1/{delivery_id}/requester/reviewed")
    suspend fun getReviewedRequester(
        @Path("delivery_id") deliveryId: Long,
    ): DeliveryReviewDto

    @POST("api/deliverys/v1/search/requester") //검색하기
    suspend fun postDeliverySearch(
        @Body body: DeliverySearchReq
    ): DeliverySearchResponse

    @POST("api/deliverys/v1/{delivery_id}/product_picture") //사진한장등록
    suspend fun postDeliveryPicture(
        @Path("delivery_id") delivery_id: Long,
        @Body body: DeliveryRegPhoto
    ): Boolean

    @POST("api/deliverys/v1/{delivery_id}/product_pictures")//사진 여러장등록
    suspend fun postDeliveryPictures(
        @Path("delivery_id") delivery_id: Long,
        @Body body: List<DeliveryRegPhoto>
    ): Boolean

    @PUT("api/deliverys/v1/{delivery_id}/player/complete_picture") //동행완료 촬영하기
    suspend fun putDeliveryCompletePicture(
        @Path("delivery_id") delivery_id: Long,
        @Body body: DeliveryUploadPictureRequest
    ): Boolean

    @PUT("api/deliverys/v1/{delivery_id}/player/pickup_done_picture")
    suspend fun putPickupDonePicture(
        @Path("delivery_id") deliveryId: Long,
        @Body body: DeliveryUploadPictureRequest,
    ): Boolean

    @GET("api/deliverys/v1/{delivery_id}/player/review/check")
    suspend fun checkReviewPlayer(
        @Path("delivery_id") deliveryId: Long,
    ): ReviewCheckDto

    @POST("api/deliverys/v1/{delivery_id}/player/review")
    suspend fun writeReviewPlayer(
        @Path("delivery_id") deliveryId: Long,
        @Body body: DeliveryReviewRequest,
    ): Boolean

    @GET("api/deliverys/v1/{delivery_id}/player/reviewed")
    suspend fun getReviewedPlayer(
        @Path("delivery_id") deliveryId: Long,
    ): DeliveryReviewDto

    @PUT("api/deliverys/v1/{delivery_id}/requester/cancel")//취소하기
    suspend fun cancelDelivery(
        @Path("delivery_id") delivery_id: Long,
    ): Boolean

    @PUT("api/deliverys/v1/{delivery_id}/requester/delete")
    suspend fun deleteDelivery(
        @Path("delivery_id") deliveryId: Long,
    ): Boolean

    //플레이어
    @POST("api/deliverys/v1/search/player") //검색하기
    suspend fun postPlayerDeliverySearch(
        @Body body: DeliverySearchReq
    ): DeliverySearchResponse

    @POST("api/deliverys/v1/search/player/likes") //좋아요 한 배송요청 검색
    suspend fun postPlayerDeliveryLikeSearch(
        @Body body: DeliverySearchReq
    ): DeliverySearchResponse

    @PUT("/api/deliverys/v1/{delivery_id}/player/apply") //지원하기
    suspend fun putApply(
        @Path("delivery_id") deliveryId: Long,
    ):Boolean

    @PUT("/api/deliverys/v1/{delivery_id}/player/apply_cancel") //지원 취소하기
    suspend fun putApplyCancel(
        @Path("delivery_id") deliveryId: Long,
    ): Boolean

    @POST("api/deliverys/v1/{delivery_id}/player/like") //좋아요 설정
    suspend fun postLikeDelivery(
        @Path("delivery_id") deliveryId: Long,
    ): Boolean

    @POST("api/deliverys/v1/{delivery_id}/player/unlike") //좋아요 해제
    suspend fun postUnlikeDelivery(
        @Path("delivery_id") deliveryId: Long,
    ): Boolean

    @PUT("api/deliverys/v1/{delivery_id}/player/chat") //채팅하기
    suspend fun putPlayerChat(
        @Path("delivery_id") deliveryId: Long,
    ): DeliveryPlayerChatDto

}
