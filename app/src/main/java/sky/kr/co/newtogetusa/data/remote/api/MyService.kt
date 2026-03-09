package sky.kr.co.newtogetusa.data.remote.api

import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path
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

}