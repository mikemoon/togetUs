package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.MyService
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class MyRepository @Inject constructor(
    @NetworkModule.MyApi private val apiService: MyService
): BaseNetRepo() {

    suspend fun putNoticeList(hashMap: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.putNoticeList(hashMap)
    }

    suspend fun putNoticeDetail(noticeId: Int) = safeApiCall(Dispatchers.IO){
        apiService.putNoticeDetail(noticeId)
    }

    suspend fun putFaqCateList() = safeApiCall(Dispatchers.IO){
        apiService.putFAQCateList()
    }

    suspend fun putFaqList(hashMap: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.putFAQList(hashMap)
    }

    suspend fun putFaqDetail(faqId: Int) = safeApiCall(Dispatchers.IO){
        apiService.putFAQDetail(faqId)
    }

    suspend fun putOneOnOne() = safeApiCall(Dispatchers.IO) {
        apiService.putOneOnOne()
    }

    suspend fun postOneOnOne(hashMap: HashMap<String, String>) = safeApiCall(Dispatchers.IO){
        apiService.postOneOnOne(hashMap)
    }

    suspend fun deleteWithdraw() = safeApiCall(Dispatchers.IO){
        apiService.deleteWithdraw()
    }

}