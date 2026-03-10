package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQCateDto
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQDetailDto
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQDto
import sky.kr.co.newtogetusa.data.remote.dto.my.InquiryDto
import sky.kr.co.newtogetusa.data.remote.dto.my.NoticeDto

interface MyService {
    @PUT("/api/mys/v1/notice")
    suspend fun putNoticeList(
        @Body request: HashMap<String, String>
    ):List<NoticeDto>

    @PUT("/api/mys/v1/notice/{notice_id}")
    suspend fun putNoticeDetail(
        @Path("notice_id") noticeId: Int
    ):NoticeDto

    @PUT("/api/mys/v1/faq_cate")
    suspend fun putFAQCateList(
    ):List<FAQCateDto>

    @PUT("/api/mys/v1/faq")
    suspend fun putFAQList(
        @Body request: HashMap<String, String>
    ):List<FAQDto>

    @PUT("/api/mys/v1/faq/{faq_id}")
    suspend fun putFAQDetail(
        @Path("faq_id") faqId: Int
    ): FAQDetailDto

    @PUT("/api/mys/v1/1n1")
    suspend fun putOneOnOne(
    ):List<InquiryDto>

    @POST("/api/mys/v1/1n1")
    suspend fun postOneOnOne(
        @Body request: HashMap<String, String>
    ): Boolean


}