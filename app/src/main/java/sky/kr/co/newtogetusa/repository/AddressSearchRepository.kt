package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.AddressSearchService
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class AddressSearchRepository @Inject constructor(@NetworkModule.AddressApiServer private val apiService: AddressSearchService):BaseNetRepo(){

    suspend fun searchAddress(query: String, page: Int? = null, size: Int? = null) = safeApiCall(Dispatchers.IO) {
        apiService.search(query, page, size)
    }
}