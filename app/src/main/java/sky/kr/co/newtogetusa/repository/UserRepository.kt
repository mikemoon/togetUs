package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.UserService
import sky.kr.co.newtogetusa.data.remote.request.user.ProfileImageRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class UserRepository @Inject constructor(
    @NetworkModule.UserApi private val apiService: UserService
) :BaseNetRepo(){

    suspend fun getMyProfile() = safeApiCall(Dispatchers.IO){
        apiService.getMyProfile()
    }

    suspend fun getProfile(userId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getProfile(userId)
    }

    suspend fun putProfileNickname(userId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.putProfileNickname(userId, request)
    }

    suspend fun putProfileImage(userId: Int, request: ProfileImageRequest) = safeApiCall(Dispatchers.IO){
        apiService.putProfileImage(userId, request)
    }

    suspend fun postReportUser(userId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReportUser(userId, request)
    }

    suspend fun postBlockUser(userId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postBlockUser(userId, request)
    }

    suspend fun getReviews(userId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getReviews(userId)
    }

    suspend fun postReview(userId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReview(userId, request)
    }

    suspend fun getBanners() = safeApiCall(Dispatchers.IO){
        apiService.getBanners()
    }

    suspend fun getBannerDetail(bnId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getBannerDetail(bnId)
    }

}