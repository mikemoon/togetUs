package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddedRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddRequest
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
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

    private val _suggestDeliveryResult = SingleLiveEvent<Boolean>()
    val suggestDeliveryResult: LiveData<Boolean> = _suggestDeliveryResult

    private val _areaAddResult = SingleLiveEvent<Boolean>()
    val areaAddResult: LiveData<Boolean> = _areaAddResult

    val deliveryRequestTotalCount = MutableStateFlow(0)
    val playerReviewCount = MutableStateFlow(0)

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

    fun addPlayerAreaAdded(request: PlayerAreaAddedRequest) = viewModelScope.launch {
        val playerId = profileDto.value?.user?.player_id ?: return@launch
        when (playerRepository.postPlayerAreaAdded(playerId, request)) {
            is ResultWrapper.Success -> _areaAddResult.value = true
            else -> _areaAddResult.value = false
        }
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
    }

    sealed class TopMenu {
        object All : TopMenu()
        object Doing : TopMenu()
        object End : TopMenu()
    }
}
