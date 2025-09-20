package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.data.remote.dto.users.req.ProfileImageRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class PlayerRepository @Inject constructor(
    @NetworkModule.PlayerApi private val apiService: PlayerService
) : BaseNetRepo(){

    suspend fun postPlayer(request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postPlayer(request)
    }

    suspend fun putProfileImage(playerId: Int, request: ProfileImageRequest) = safeApiCall(Dispatchers.IO){
        apiService.putProfileImage(playerId, request)
    }

    suspend fun postIntroduction(playerId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postIntroduction(playerId, request)
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



}