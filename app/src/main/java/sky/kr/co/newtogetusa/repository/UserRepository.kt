package sky.kr.co.newtogetusa.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.UserService
import sky.kr.co.newtogetusa.data.remote.dto.users.DeliveryItem
import sky.kr.co.newtogetusa.data.remote.request.user.DeliveryListSearchRequest
import sky.kr.co.newtogetusa.data.remote.request.user.ProfileImageRequest
import sky.kr.co.newtogetusa.di.NetworkModule
import sky.kr.co.newtogetusa.repository.page.DeliveryRequestSearchPagingSource
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

    suspend fun unblockUser(userId: Int) = safeApiCall(Dispatchers.IO) {
        apiService.unblockUser(userId)
    }

    suspend fun getBlockedUsers() = safeApiCall(Dispatchers.IO) {
        apiService.getBlockedUsers()
    }

    suspend fun getReviews(userId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getReviews(userId)
    }

    suspend fun postReview(userId: Int, request: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postReview(userId, request)
    }

    suspend fun postUsersSearch(request: DeliveryListSearchRequest) = safeApiCall(Dispatchers.IO){
        apiService.postUsersSearch(request)
    }

    fun getDeliveryRequestSearchPagingFlow(
        myArea: Boolean,
        departCd: List<String>,
        destCd: List<String>,
        sortType: String,
        fee: Int,
        face2Face: Boolean?,
        immediately: String?,
        pageSize: Int = 10
    ): Flow<PagingData<DeliveryItem>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                DeliveryRequestSearchPagingSource(
                    userRepository = this,
                    myArea = myArea,
                    departCd = departCd,
                    destCd = destCd,
                    sortType = sortType,
                    fee = fee,
                    face2Face = face2Face,
                    immediately = immediately,
                    pageSize = pageSize
                )
            }
        ).flow

    suspend fun getBanners() = safeApiCall(Dispatchers.IO){
        apiService.getBanners()
    }

    suspend fun getBannerDetail(bnId: Int) = safeApiCall(Dispatchers.IO){
        apiService.getBannerDetail(bnId)
    }

}
