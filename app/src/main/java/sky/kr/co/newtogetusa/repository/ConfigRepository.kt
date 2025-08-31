package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.ConfigService
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class ConfigRepository @Inject constructor(
    @NetworkModule.ConfigApi private val apiService: ConfigService
) : BaseNetRepo() {

    suspend fun getDomesticAreas() = safeApiCall<List<RegionDto>>(Dispatchers.IO){
        apiService.getDomesticAreas()
    }

    suspend fun getDomesticSubAreas() = safeApiCall<List<RegionDto>>(Dispatchers.IO){
        apiService.getDomesticSubAreas()
    }
}