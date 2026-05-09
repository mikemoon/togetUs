package sky.kr.co.newtogetusa.ui.main.chat

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomDto
import sky.kr.co.newtogetusa.data.remote.dto.chat.ChatRoomSearchRoomDto
import sky.kr.co.newtogetusa.data.remote.request.chat.ChatRoomSearchRequest
import sky.kr.co.newtogetusa.repository.ChatRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChattingTabViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val chatRepository: ChatRepository) :
    BaseViewModel(baseViewModelFactory.create()) {

    val chatRooms = MutableStateFlow<List<ChatRoomDto>>(emptyList())
    private val playerRoomRequest = MutableStateFlow(defaultRequest())
    private val userRoomRequest = MutableStateFlow(defaultRequest())

    val playerRoomPagingData = playerRoomRequest
        .flatMapLatest { request ->
            chatRepository.getPlayerRoomPagingFlow(
                type = request.type,
                pageSize = request.page_size
            )
        }
        .cachedIn(viewModelScope)

    val userRoomPagingData = userRoomRequest
        .flatMapLatest { request ->
            chatRepository.getUserRoomPagingFlow(
                type = request.type,
                pageSize = request.page_size
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
        playerRoomRequest.value = request
    }

    fun searchUserRooms(request: ChatRoomSearchRequest) {
        userRoomRequest.value = request
    }

    companion object {
        const val TYPE_ALL = "ALL"
        const val TYPE_PROGRESS = "PROGRESS"
        const val TYPE_END = "END"

        fun defaultRequest() = ChatRoomSearchRequest(
            type = TYPE_ALL,
            page_no = 0,
            page_size = 10
        )
    }

}
