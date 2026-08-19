package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.MyService
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class MyRepository @Inject constructor(
    @NetworkModule.MyApi private val apiService: MyService
): BaseNetRepo() {

    suspend fun getNotifications(pageNo: Int, pageSize: Int) = safeApiCall(Dispatchers.IO) {
        apiService.getNotifications(pageNo, pageSize)
    }

    suspend fun getNotificationUnreadCount() = safeApiCall(Dispatchers.IO) {
        apiService.getNotificationUnreadCount()
    }

    suspend fun putNotificationsReadAll() = safeApiCall(Dispatchers.IO) {
        apiService.putNotificationsReadAll()
    }

    suspend fun putNotificationRead(notificationId: Long) = safeApiCall(Dispatchers.IO) {
        apiService.putNotificationRead(notificationId)
    }

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

    suspend fun postOneOnOne(inquiry: String, phone: String, imageBytesList: List<ByteArray>) = safeApiCall(Dispatchers.IO){
        val textMime = "text/plain".toMediaType()
        val attaches = imageBytesList.mapIndexed { index, bytes ->
            MultipartBody.Part.createFormData(
                "attaches",
                "inquiry_$index.jpg",
                bytes.toRequestBody("image/jpeg".toMediaType())
            )
        }
        apiService.postOneOnOne(
            inquiry = inquiry.toRequestBody(textMime),
            phone = phone.toRequestBody(textMime),
            attaches = attaches
        )
    }

    suspend fun deleteWithdraw() = safeApiCall(Dispatchers.IO){
        apiService.deleteWithdraw()
    }

}
