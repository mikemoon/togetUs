package sky.kr.co.newtogetusa.repository.page

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerSearchRequest
import sky.kr.co.newtogetusa.repository.PlayerRepository

class PlayerSearchPagingSource(
    private val apiService: PlayerService,
    private val departCd: List<String>,
    private val destCd: List<String>,
    private val sortType: String
) : PagingSource<Int, PlayerDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlayerDto> {
        val page = params.key ?: 0

        return try {

            val request = PlayerSearchRequest(
                depart_cd = departCd,
                dest_cd = destCd,
                sort_type = sortType,
                page_no = page,
                page_size = params.loadSize
            )

            val response = apiService.postPlayersSearch(request)

            LoadResult.Page(
                data = response.players,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.has_more) page + 1 else null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PlayerDto>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}