package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddedRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaLocationRequest
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.chat.ChatRoomListUpdateBus
import javax.inject.Inject

@HiltViewModel
class ProfileManagementViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                     private val userRepository: UserRepository,
                                                     private val playerRepository: PlayerRepository,
                                                     private val configRepository: ConfigRepository,
    private val deliveryRepository: DeliveryRepository
)
    : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val profileDto = MutableStateFlow<ProfileDto?>(null)
    fun getMyProfile(result: (ProfileDto) -> Unit)= viewModelScope.launch {
        when(val response = userRepository.getMyProfile()){
            is ResultWrapper.Success ->{
                dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                profileDto.value = response.data
                result.invoke(response.data)
            }
            else ->{}
        }
    }

    fun getPlayerProfile(result: (PlayerProfileDto) -> Unit = {}) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when(val res = playerRepository.getProfile(playerId)){
            is ResultWrapper.Success ->{
                result.invoke(res.data)
            }
            else -> {}
        }
    }

    fun getPlayerReviews(result: (List<PlayerInfoDto>) -> Unit = {}) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when(val res = playerRepository.getReviews(playerId)){
            is ResultWrapper.Success ->{
                playerReviewCount.value = res.data.size
                result.invoke(res.data)
            }
            else -> {

            }
        }
    }

    fun getPlayerReviewCodes(result: (List<BaseCommonDto>) -> Unit = {}) = viewModelScope.launch {
        when(val res = configRepository.getPlayerReview()){
            is ResultWrapper.Success ->{
                result.invoke(res.data)
            }
            else -> {}
        }
    }

    val menuAll = TopMenu.All
    val menuDoing = TopMenu.Doing
    val menuEnd = TopMenu.End

    val eventBack = Event.Back
    val eventModifyProfile = Event.ModifyProfile
    val eventSuggestDelivery = Event.SuggestDelivery
    val eventMore = Event.More
    val eventToggleLike = Event.ToggleLike

    private val _suggestDeliveryResult = SingleLiveEvent<Boolean>()
    val suggestDeliveryResult: LiveData<Boolean> = _suggestDeliveryResult

    private val _areaAddResult = SingleLiveEvent<Boolean>()
    val areaAddResult: LiveData<Boolean> = _areaAddResult

    val deliveryRequestTotalCount = MutableStateFlow(0)
    val playerReviewCount = MutableStateFlow(0)
    private val _isPlayerLiked = MutableStateFlow(false)
    val isPlayerLiked = _isPlayerLiked.asStateFlow()
    private val _profileActionMessage = MutableSharedFlow<String>()
    val profileActionMessage = _profileActionMessage.asSharedFlow()

    private val _topMenu = MutableStateFlow<TopMenu>(TopMenu.All)
    private val _topMenuLiveData = SingleLiveEvent<TopMenu>()
    val topMenuLiveData: LiveData<TopMenu> = _topMenuLiveData

    fun onTopMenuSelect(topMenu: TopMenu) {
        _topMenu.value = topMenu
        _topMenuLiveData.value = topMenu
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val deliveryPagingFlow = _topMenu
        .flatMapLatest { selectedMenu ->
            deliveryRepository.getDeliveryPagingFlow(type = "ALL", title = "")
                .map { pagingData ->
                    pagingData.filter { item ->
                        when (selectedMenu) {
                            is TopMenu.All -> true
                            is TopMenu.Doing -> item.status_cd.startsWith("ING") ||
                                    item.status_cd.startsWith("REG") ||
                                    item.status_cd.startsWith("MATCH") ||
                                    item.status_cd.startsWith("DELIVERY")
                            is TopMenu.End -> item.status_cd.startsWith("DONE") || item.status_cd.startsWith("CANCEL")
                        }
                    }
                }
        }
        .cachedIn(viewModelScope)


    fun fetchDeliveryRequestTotalCount() = viewModelScope.launch {
        when (val res = deliveryRepository.postDeliverySearch(
            DeliverySearchReq(
                type = "ALL",
                title = "",
                page_no = 0
            )
        )) {
            is ResultWrapper.Success -> {
                deliveryRequestTotalCount.value = res.data.total_cnt
            }
            else -> {}
        }
    }


    fun requestSuggestDelivery(deliveryId: Long) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: -1
        if (playerId <= 0L || deliveryId <= 0L) {
            _suggestDeliveryResult.value = false
            return@launch
        }

        when (deliveryRepository.putSuggest(deliveryId = deliveryId, hashMapOf("player_id" to playerId.toString()))) {
            is ResultWrapper.Success -> _suggestDeliveryResult.value = true
            else -> _suggestDeliveryResult.value = false
        }
    }

    fun addPlayerArea(request: PlayerAreaAddRequest) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when (playerRepository.postPlayerArea(playerId, request)) {
            is ResultWrapper.Success -> _areaAddResult.value = true
            else -> _areaAddResult.value = false
        }
    }

    fun setPlayerLiked(isLiked: Boolean) {
        _isPlayerLiked.value = isLiked
    }

    fun togglePlayerLike() = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        val nextLiked = !_isPlayerLiked.value
        val result = if (nextLiked) {
            playerRepository.postLikePlayer(playerId)
        } else {
            playerRepository.postUnlikePlayer(playerId)
        }

        when (result) {
            is ResultWrapper.Success -> _isPlayerLiked.value = nextLiked
            else -> _profileActionMessage.emit("좋아요 처리에 실패했습니다.")
        }
    }

    fun blockPlayer() = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when (playerRepository.postBlockPlayer(playerId, hashMapOf("block_cd" to "BLOCK"))) {
            is ResultWrapper.Success -> {
                // 차단 후 이 상대와 연결된 모든 채팅방 상태가 변경될 수 있다.
                ChatRoomListUpdateBus.notifyAllRoomsUpdated()
                _profileActionMessage.emit("차단되었습니다.")
            }
            else -> _profileActionMessage.emit("차단 처리에 실패했습니다.")
        }
    }

    fun reportPlayer() = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when (playerRepository.postReportPlayer(playerId, hashMapOf("report_cd" to "REPORT", "content" to ""))) {
            is ResultWrapper.Success -> _profileActionMessage.emit("신고가 접수되었습니다.")
            else -> _profileActionMessage.emit("신고 접수에 실패했습니다.")
        }
    }

    fun addPlayerAreaAdded(request: PlayerAreaAddedRequest) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when (playerRepository.postPlayerAreaAdded(playerId, request)) {
            is ResultWrapper.Success -> _areaAddResult.value = true
            else -> _areaAddResult.value = false
        }
    }

    fun setPlayerGpsEnable(enable: Boolean, result: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch result(false)
        loadingState.value = true
        val success = playerRepository.setPlayerGpsEnable(playerId, enable) is ResultWrapper.Success
        loadingState.value = false
        result(success)
    }

    fun deletePlayerArea(areaId: Int, result: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch result(false)
        loadingState.value = true
        val success = playerRepository.deletePlayerArea(playerId, areaId) is ResultWrapper.Success
        loadingState.value = false
        result(success)
    }

    fun savePlayerBasicAreas(
        areas: List<PlayerBasicAreaEdit>,
        removedAreaIds: List<Int>,
        result: (Boolean) -> Unit = {}
    ) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch result(false)
        loadingState.value = true

        for (areaId in removedAreaIds) {
            if (playerRepository.deletePlayerArea(playerId, areaId) !is ResultWrapper.Success) {
                loadingState.value = false
                result(false)
                return@launch
            }
        }

        for (area in areas) {
            val depart = area.depart ?: continue
            val dest = area.dest ?: continue
            val departRadius = area.departRadius ?: continue
            val destRadius = area.destRadius ?: continue
            val areaId = area.areaId

            if (areaId != null && area.addressChanged) {
                if (playerRepository.deletePlayerArea(playerId, areaId) !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }
            }

            if (areaId != null && !area.addressChanged) {
                if (area.enableChanged) {
                    if (playerRepository.setPlayerAreaEnable(playerId, areaId, area.enable) !is ResultWrapper.Success) {
                        loadingState.value = false
                        result(false)
                        return@launch
                    }
                }
            } else {
                val addResult = playerRepository.postPlayerArea(
                    playerId,
                    PlayerAreaAddRequest(
                        area_id = null,
                        is_domestic = true,
                        enable = area.enable,
                        depart = PlayerAreaLocationRequest(
                            address = depart.name,
                            address2 = depart.roadAddress.orEmpty(),
                            latitude = depart.lat ?: 0.0,
                            longitude = depart.lng ?: 0.0,
                            range = departRadius
                        ),
                        dest = PlayerAreaLocationRequest(
                            address = dest.name,
                            address2 = dest.roadAddress.orEmpty(),
                            latitude = dest.lat ?: 0.0,
                            longitude = dest.lng ?: 0.0,
                            range = destRadius
                        )
                    )
                )
                if (addResult !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }
            }
        }

        loadingState.value = false
        result(true)
    }


    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object ModifyProfile : Event()
        object SuggestDelivery : Event()
        object PasswordSet : Event()
        object More : Event()
        object ToggleLike : Event()
    }

    sealed class TopMenu {
        object All : TopMenu()
        object Doing : TopMenu()
        object End : TopMenu()
    }

    data class PlayerBasicAreaEdit(
        val areaId: Int?,
        val addressChanged: Boolean,
        val enableChanged: Boolean,
        val enable: Boolean,
        val depart: KakaoSearchModel?,
        val departRadius: Int?,
        val dest: KakaoSearchModel?,
        val destRadius: Int?
    )
}
