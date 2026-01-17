package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.AdminApiService
import sky.kr.co.newtogetusa.data.remote.dto.admin.UserDetailResponseDto
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class AdminRepository @Inject constructor(@NetworkModule.AdminApi private val apiService: AdminApiService): BaseNetRepo(){

    suspend fun getUserDetail(userId: Int) = safeApiCall<UserDetailResponseDto>(dispatcher = Dispatchers.IO) {
        apiService.getUserDetail(userId)
    }
}