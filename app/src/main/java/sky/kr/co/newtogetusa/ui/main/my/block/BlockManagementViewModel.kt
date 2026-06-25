package sky.kr.co.newtogetusa.ui.main.my.block

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BlockManagementViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val userRepository: UserRepository,
    private val playerRepository: PlayerRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _items = MutableStateFlow<List<BlockedItem>>(emptyList())
    val items: StateFlow<List<BlockedItem>> = _items.asStateFlow()

    private val _message = SingleLiveEvent<String>()
    val message: LiveData<String> = _message

    fun loadBlockedItems() = viewModelScope.launch {
        val users = when (val response = userRepository.getBlockedUsers()) {
            is ResultWrapper.Success -> response.data.mapNotNull {
                val id = it.user_id ?: return@mapNotNull null
                BlockedItem(
                    id = id,
                    name = it.nickname.orEmpty().ifBlank { "사용자" },
                    profileImage = it.profile_image,
                    type = BlockedItem.Type.USER
                )
            }
            else -> emptyList()
        }

        val players = when (val response = playerRepository.getBlockedPlayers()) {
            is ResultWrapper.Success -> response.data.players.map {
                BlockedItem(
                    id = it.player_id.toInt(),
                    name = it.nickname,
                    profileImage = it.profile_image,
                    type = BlockedItem.Type.PLAYER
                )
            }
            else -> emptyList()
        }

        _items.value = users + players
    }

    fun unblock(item: BlockedItem) = viewModelScope.launch {
        val result = when (item.type) {
            BlockedItem.Type.USER -> userRepository.unblockUser(item.id)
            BlockedItem.Type.PLAYER -> playerRepository.unblockPlayer(item.id)
        }

        when (result) {
            is ResultWrapper.Success -> {
                _items.value = _items.value.filterNot { it.type == item.type && it.id == item.id }
                _message.value = "차단이 해제되었습니다."
            }
            else -> _message.value = "차단 해제에 실패했습니다."
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
    }
}
