package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.ConfigService
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.data.remote.request.config.NotificationSettingReq
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

    suspend fun getProductWeightList() = safeApiCall(Dispatchers.IO){
        apiService.getWeight()
    }

    suspend fun getProductVolumeList() = safeApiCall(Dispatchers.IO){
        apiService.getProductVolume()
    }

    suspend fun getProductTypeList() = safeApiCall(Dispatchers.IO){
        apiService.getProductType()
    }

    suspend fun getBanks() = safeApiCall(Dispatchers.IO){
        apiService.getBanks()
    }

    suspend fun getPlayerTerms() = safeApiCall(Dispatchers.IO){
        apiService.getPlayerTerms()
    }

    suspend fun postPushToken(body: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postPushToken(body)
    }

    suspend fun deletePushToken(body: HashMap<String, Any?>) = safeApiCall(Dispatchers.IO){
        apiService.deletePushToken(body)
    }

    suspend fun getAlarmSettings() = safeApiCall(Dispatchers.IO){
        apiService.getAlarmSettings()
    }

    suspend fun putAlarmSettings(body: NotificationSettingReq) = safeApiCall(Dispatchers.IO){
        apiService.putAlarmSettings(body)
    }

}