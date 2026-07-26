package sky.kr.co.newtogetusa.ui.main.my.likeplayer

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.search.PlayerDto
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class LikePlayerViewModel @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository
) : BaseViewModel(baseViewModelFactory.create()) {

    private val _players = MutableStateFlow<List<PlayerDto>>(emptyList())
    val players: StateFlow<List<PlayerDto>> = _players

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event

    init {
        fetchLikes()
    }

    fun fetchLikes() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val res = playerRepository.getPlayerLikes()) {
                is ResultWrapper.Success -> {
                    _players.value = res.data.players
                }
                is ResultWrapper.GenericError -> {
                    _event.value = Event.ShowMessage(res.message ?: "목록을 불러올 수 없습니다.")
                }
                is ResultWrapper.NetworkError -> {
                    _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
                }
            }
            _isLoading.value = false
        }
    }

    fun unlikePlayer(playerId: Long) {
        viewModelScope.launch {
            when (val res = playerRepository.postUnlikePlayer(playerId.toInt())) {
                is ResultWrapper.Success -> {
                    if (res.data) {
                        _players.value = _players.value.filter { it.player_id != playerId }
                        _event.value = Event.ShowMessage("좋아요가 취소되었습니다.")
                    }
                }
                is ResultWrapper.GenericError -> {
                    _event.value = Event.ShowMessage(res.message ?: "취소에 실패했습니다.")
                }
                is ResultWrapper.NetworkError -> {
                    _event.value = Event.ShowMessage("네트워크 연결을 확인해 주세요.")
                }
            }
        }
    }

    fun consumeEvent() {
        _event.value = null
    }

    sealed class Event {
        data class ShowMessage(val message: String) : Event()
    }
}
