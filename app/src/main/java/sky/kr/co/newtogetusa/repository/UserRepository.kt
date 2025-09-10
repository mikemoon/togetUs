package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.UserService
import sky.kr.co.newtogetusa.data.remote.dto.users.req.ProfileImageRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class UserRepository @Inject constructor(
    @NetworkModule.UserApi private val apiService: UserService
) :BaseNetRepo(){

    suspend fun getMyProfile() = safeApiCall(Dispatchers.IO){
        apiService.getMyProfile()
    }

    suspend fun putProfileNickname(userId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.putProfileNickname(userId, request)
    }

    suspend fun putProfileImage(userId: Int, request: ProfileImageRequest) = safeApiCall(Dispatchers.IO){
        apiService.putProfileImage(userId, request)
    }
}