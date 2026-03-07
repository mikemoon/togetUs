package sky.kr.co.newtogetusa.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerJoinRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.di.NetworkModule
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

    suspend fun postIntroduction(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postIntroduction(playerId, request)
    }

    suspend fun postCriminalRecord(playerId: Int, file: MultipartBody.Part) = safeApiCall(Dispatchers.IO) {
        apiService.postCriminalRecord(playerId, file)
    }

    suspend fun postPlayerArea(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerArea(playerId, request)
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


    suspend fun postReqDelivery(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReqDelivery(playerId, request)
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

    suspend fun verifyImpUid(request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.verifyImpUid(request)
    }

    suspend fun postPlayerBank(playerId: Int, request: BankRequestDto) = safeApiCall(Dispatchers.IO){
        apiService.postPlayerBank(playerId, request)
    }

    fun searchPlayers(
        departCd: List<String>,
        destCd: List<String>,
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
                    sortType
                )
            }
        ).flow
    }

}