package sky.kr.co.newtogetusa.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddedRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerDeliveryHistoryReq
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaDeleteRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerJoinRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerSearchRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import sky.kr.co.newtogetusa.repository.page.PlayerDeliveryHistoryPagingSource
import sky.kr.co.newtogetusa.repository.page.PlayerSearchPagingSource
import javax.inject.Inject

class PlayerRepository @Inject constructor(
    @NetworkModule.PlayerApi private val apiService: PlayerService
) : BaseNetRepo(){

    suspend fun postPlayer(request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postPlayer(request)
    }

    suspend fun postProfileImage(playerId: Int, file: MultipartBody.Part) = safeApiCall(Dispatchers.IO){
        apiService.postProfileImage(playerId, file)
    }

    suspend fun postProfileImage(playerId: Int, request: PlayerProfileImageRequest) = safeApiCall(Dispatchers.IO){
        apiService.postProfileImage(playerId, request)
    }

    suspend fun postIntroduction(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postIntroduction(playerId, request)
    }

    suspend fun postCriminalRecord(playerId: Int, file: MultipartBody.Part) = safeApiCall(Dispatchers.IO) {
        apiService.postCriminalRecord(playerId, file)
    }

    suspend fun postPlayerArea(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerArea(playerId, request)
    }

    suspend fun postPlayerArea(playerId: Int, request: PlayerAreaAddRequest) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerArea(playerId, request)
    }

    suspend fun postPlayerAreaAdded(playerId: Int, request: PlayerAreaAddedRequest) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerAreaAdded(playerId, request)
    }

    suspend fun deletePlayerArea(playerId: Int, areaId: Int) = safeApiCall(Dispatchers.IO){
        apiService.deletePlayerArea(playerId, PlayerAreaDeleteRequest(areaId))
    }

    suspend fun setPlayerGpsEnable(playerId: Int, enable: Boolean): ResultWrapper<Boolean> = safeApiCall(Dispatchers.IO) {
        apiService.setPlayerGpsEnable(playerId, hashMapOf("enable" to enable))
    }

    suspend fun setPlayerAreaEnable(playerId: Int, areaId: Int, enable: Boolean): ResultWrapper<Boolean> = safeApiCall(Dispatchers.IO) {
        apiService.setPlayerAreaEnable(
            playerId,
            hashMapOf(
                "area_id" to areaId,
                "enable" to enable
            )
        )
    }

    suspend fun refreshPlayerGps(playerId: Int, latitude: Double, longitude: Double) = safeApiCall(Dispatchers.IO) {
        apiService.refreshPlayerGps(
            playerId,
            hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )
        )
    }

    suspend fun getProfile(playerId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getProfile(playerId)
    }

    suspend fun postReportPlayer(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReportPlayer(playerId, request)
    }

    suspend fun postBlockPlayer(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postBlockPlayer(playerId, request)
    }

    suspend fun unblockPlayer(playerId: Int) = safeApiCall(Dispatchers.IO) {
        apiService.unblockPlayer(playerId)
    }

    suspend fun getBlockedPlayers() = safeApiCall(Dispatchers.IO) {
        apiService.getBlockedPlayers()
    }

    suspend fun getPlayerLikes() = safeApiCall(Dispatchers.IO) {
        apiService.getPlayerLikes()
    }

    suspend fun postReqDelivery(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReqDelivery(playerId, request)
    }

    suspend fun postLikePlayer(playerId: Int) = safeApiCall(Dispatchers.IO) {
        apiService.postLikePlayer(playerId)
    }

    suspend fun postUnlikePlayer(playerId: Int) = safeApiCall(Dispatchers.IO) {
        apiService.postUnlikePlayer(playerId)
    }

    suspend fun getReviews(playerId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getReviews(playerId)
    }

    suspend fun postReview(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReview(playerId, request)
    }

    suspend fun getPlayers() = safeApiCall(Dispatchers.IO){
        apiService.getPlayers()
    }

    suspend fun postPlayerApplyBatch(playerId: Int, dataRequestBody: RequestBody, profilePart: MultipartBody.Part?, criminalPart: MultipartBody.Part?) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerApplyBatch(
            playerId = playerId,
            data = dataRequestBody,
            profile_image = profilePart,
            criminal_record = criminalPart
        )
    }

    suspend fun postPlayerApply(playerId: Int) = safeApiCall(Dispatchers.IO) {
        apiService.postPlayerApply(playerId, hashMapOf())
    }

    suspend fun cancelPlayerApplication(playerId: Int) = safeApiCall(Dispatchers.IO) {
        apiService.cancelPlayerApplication(playerId)
    }

    suspend fun getPortOneConfig() = safeApiCall(Dispatchers.IO) {
        apiService.getPortOneConfig()
    }

    suspend fun verifyIdentity(identityVerificationId: String) = safeApiCall(Dispatchers.IO) {
        apiService.verifyIdentity(
            hashMapOf(
                "identity_verification_id" to identityVerificationId,
                "os" to "A"
            )
        )
    }

    suspend fun postPlayerBank(playerId: Int, request: BankRequestDto) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerBank(playerId, request)
    }

    fun searchPlayers(
        departCd: List<String>,
        destCd: List<String>,
        isDomestic: Boolean,
        sortType: String
    ): Flow<PagingData<PlayerDto>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PlayerSearchPagingSource(
                    apiService,
                    departCd,
                    destCd,
                    isDomestic,
                    sortType
                )
            }
        ).flow
    }

    fun getPlayerDeliveryHistoryPagingFlow(type: String, title: String, pageSize: Int = 30) =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PlayerDeliveryHistoryPagingSource(this, type, title, pageSize)
            }
        ).flow

    suspend fun postPlayerDeliveryList(request: PlayerDeliveryHistoryReq) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerDeliveryList(request)
    }

    suspend fun postPlayersSearch(request: PlayerSearchRequest) = safeApiCall(Dispatchers.IO){
        apiService.postPlayersSearch(request)
    }

    suspend fun getMonthInfo(year: Int, month: Int) = safeApiCall(Dispatchers.IO) {
        apiService.getMonthInfo(year, month)
    }

    suspend fun getDayInfo(year: Int, month: Int, day: Int) = safeApiCall(Dispatchers.IO){
        apiService.getDayInfo(year, month, day)
    }

}
