package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchRoomDto
import sky.kr.co.newtogetusa.data.remote.request.chat.ChatRoomSearchRequest
import sky.kr.co.newtogetusa.repository.ChatRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChattingTabViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val chatRepository: ChatRepository,
    private val playerRepository: PlayerRepository) :
    BaseViewModel(baseViewModelFactory.create()) {

    val chatRooms = MutableStateFlow<List<ChatRoomDto>>(emptyList())
    val chatTabUiState = MutableStateFlow(ChatTabUiState())
    val selectedTopTabPosition = MutableStateFlow<Int?>(null)
    private val playerRoomSearch = MutableStateFlow(RoomSearch(defaultRequest()))
    private val userRoomSearch = MutableStateFlow(RoomSearch(defaultRequest()))

    init {
        observePlayerMode()
        refreshPlayerApproval()
    }

    val playerRoomPagingData = playerRoomSearch
        .flatMapLatest { search ->
            chatRepository.getPlayerRoomPagingFlow(
                type = search.request.type,
                pageSize = search.request.page_size
            )
        }
        .cachedIn(viewModelScope)

    val userRoomPagingData = userRoomSearch
        .flatMapLatest { search ->
            chatRepository.getUserRoomPagingFlow(
                type = search.request.type,
                pageSize = search.request.page_size
            )
        }
        .cachedIn(viewModelScope)

    fun getChatRooms() = viewModelScope.launch {
        when (val response = chatRepository.getChatRooms()) {
            is ResultWrapper.Success -> {
                chatRooms.value = response.data
            }
            else -> Unit
        }
    }

    fun searchPlayerRooms(request: ChatRoomSearchRequest) {
        playerRoomSearch.value = playerRoomSearch.value.next(request)
    }

    fun searchUserRooms(request: ChatRoomSearchRequest) {
        userRoomSearch.value = userRoomSearch.value.next(request)
    }

    fun refreshRoomsForTab(isPlayer: Boolean) {
        if (isPlayer) {
            playerRoomSearch.value = playerRoomSearch.value.next()
        } else {
            userRoomSearch.value = userRoomSearch.value.next()
        }
    }

    fun selectTopTab(position: Int) {
        selectedTopTabPosition.value = position
    }

    fun refreshPlayerApproval() = viewModelScope.launch {
        when (val response = playerRepository.getPlayers()) {
            is ResultWrapper.Success -> {
                chatTabUiState.value = chatTabUiState.value.copy(
                    isApprovedPlayer = !response.data.certi_res_date.isNullOrEmpty()
                )
            }
            is ResultWrapper.GenericError -> {
                if (response.code?.toIntOrNull() == 404) {
                    chatTabUiState.value = chatTabUiState.value.copy(isApprovedPlayer = false)
                }
            }
            else -> Unit
        }
    }

    private fun observePlayerMode() = viewModelScope.launch {
        dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER)
            .filterNotNull()
            .collectLatest { isPlayerMode ->
                chatTabUiState.value = chatTabUiState.value.copy(isPlayerMode = isPlayerMode)
            }
    }

    data class ChatTabUiState(
        val isApprovedPlayer: Boolean = false,
        val isPlayerMode: Boolean = false
    )

    companion object {
        const val TYPE_ALL = "ALL"
        const val TYPE_PROGRESS = "ING"
        const val TYPE_END = "DONE"

        fun defaultRequest() = ChatRoomSearchRequest(
            type = TYPE_ALL,
            page_no = 0,
            page_size = 10
        )
    }

    private data class RoomSearch(
        val request: ChatRoomSearchRequest,
        val refreshId: Long = 0L
    ) {
        fun next(request: ChatRoomSearchRequest = this.request): RoomSearch =
            copy(request = request, refreshId = refreshId + 1)
    }

}
