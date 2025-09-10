package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class PlayerRepository @Inject constructor(
    @NetworkModule.PlayerApi private val apiService: PlayerService
) : BaseNetRepo(){

    suspend fun postPlayer(request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postPlayer(request)
    }
}