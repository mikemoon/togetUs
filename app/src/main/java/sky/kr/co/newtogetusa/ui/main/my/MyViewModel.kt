package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerApplyedInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartViewModel.Event
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository,
    private val userRepository: UserRepository,
) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val isModeChanging = MutableStateFlow(false)
    val isPlayerModeFlow = MutableStateFlow(false)

    val isPlayerRequestBtnVisible = MutableStateFlow(false)
    val isPlayerModeChangeBtnVisible = MutableStateFlow(false)

    val impUidString = MutableStateFlow("")

    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                .collectLatest {
                    isPlayerModeFlow.value = it
                }
        }
        getImpUid()
    }

    val profileDto = MutableStateFlow<ProfileDto?>(null)
    private var userProfileDto: ProfileDto? = null

    fun getMyProfile(result: (ProfileDto) -> Unit) = viewModelScope.launch {
        val profileData = getUserProfile()
        if (profileData != null) {
            profileDto.value = profileData
            result.invoke(profileData)
        }
    }

    fun refreshProfileForMode(result: (ProfileDto) -> Unit) = viewModelScope.launch {
        val userProfile = getUserProfile() ?: return@launch
        if (!isPlayerModeFlow.value) {
            profileDto.value = userProfile
            result.invoke(userProfile)
            return@launch
        }

        val playerId = userProfile.user.player_id
        if (playerId <= 0) {
            profileDto.value = userProfile
            result.invoke(userProfile)
            return@launch
        }

        val approvedPlayerProfile = getApprovedPlayerProfile(playerId)
        if (approvedPlayerProfile != null) {
            val displayProfile = userProfile.withPlayerProfile(approvedPlayerProfile)
            profileDto.value = displayProfile
            result.invoke(displayProfile)
        } else {
            dataStoreRepository.putBoolean(DataStoreKey.KEY_IS_MODE_PLAYER, false)
            isPlayerModeFlow.value = false
            profileDto.value = userProfile
            result.invoke(userProfile)
        }
    }

    private suspend fun getUserProfile(): ProfileDto? {
        when (val response = userRepository.getMyProfile()) {
            is ResultWrapper.Success -> {
                dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                userProfileDto = response.data
                return response.data
            }
            else -> Unit
        }

        val profileData = dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)
        if (profileData != null) {
            Timber.d("profileAlreadyHas profileData $profileData")
            userProfileDto = profileData
            return profileData
        }

        return null
    }

    private fun ProfileDto.withPlayerProfile(playerProfile: PlayerProfileDto): ProfileDto {
        val reviewCount = playerProfile.rating?.review_count
            ?: playerProfile.evaluation?.review_count
            ?: review_count
        val starAverage = playerProfile.evaluation?.start_average
            ?: playerProfile.rating?.star_average?.toInt()
            ?: evaluation.start_average
        val cancelCount = playerProfile.evaluation?.cancel_count
            ?: playerProfile.rating?.cancel_count
            ?: evaluation.cancel_count

        return copy(
            user = user.copy(
                player_id = playerProfile.player.player_id,
                nickname = playerProfile.player.nickname,
                profile_image = playerProfile.player.profile_image,
                enable = playerProfile.player.enable
            ),
            evaluation = evaluation.copy(
                start_average = starAverage,
                cancel_count = cancelCount
            ),
            review_count = reviewCount
        )
    }

    val hasPlayerApplyRequest = MutableStateFlow(false)
    val playerApplyedInfoDto = MutableStateFlow<PlayerApplyedInfoDto?>(null)

    fun getPlayerInfo() = viewModelScope.launch {
        val myProfile = getUserProfile()
        val playerId = myProfile?.user?.player_id ?: 0
        if (playerId <= 0) {
            playerApplyedInfoDto.value = null
            applyPlayerState(
                canUsePlayerMode = false,
                showPlayerApplyButton = true,
                hasInProgressApply = false
            )
            return@launch
        }

        val approvedPlayerProfile = getApprovedPlayerProfile(playerId)
        if (approvedPlayerProfile != null) {
            val displayProfile = myProfile?.withPlayerProfile(approvedPlayerProfile)
            if (displayProfile != null) {
                profileDto.value = displayProfile
            }
            applyPlayerState(
                canUsePlayerMode = true,
                showPlayerApplyButton = false,
                hasInProgressApply = false
            )
            return@launch
        }

        when (val res = playerRepository.getPlayers()) {
            is ResultWrapper.Success -> {
                val dto = res.data
                val isUnderReview = !dto.certi_req_date.isNullOrBlank() && dto.certi_res_date.isNullOrBlank()
                playerApplyedInfoDto.value = dto
                applyPlayerState(
                    canUsePlayerMode = false,
                    showPlayerApplyButton = true,
                    hasInProgressApply = !isUnderReview
                )
            }
            is ResultWrapper.GenericError -> {
                playerApplyedInfoDto.value = null
                applyPlayerState(
                    canUsePlayerMode = false,
                    showPlayerApplyButton = true,
                    hasInProgressApply = false
                )
                Timber.d("GenericError $res")
            }
            is ResultWrapper.NetworkError -> {
                applyPlayerState(
                    canUsePlayerMode = false,
                    showPlayerApplyButton = true,
                    hasInProgressApply = false
                )
                Timber.d("NetworkError $res")
            }
        }
    }

    private suspend fun getApprovedPlayerProfile(playerId: Int): PlayerProfileDto? {
        return when (val response = playerRepository.getProfile(playerId)) {
            is ResultWrapper.Success -> {
                val playerProfile = response.data
                val isApprovedPlayer = playerProfile.player.player_id > 0 && playerProfile.player.enable
                playerProfile.takeIf { isApprovedPlayer }
            }
            else -> null
        }
    }

    private suspend fun applyPlayerState(
        canUsePlayerMode: Boolean,
        showPlayerApplyButton: Boolean,
        hasInProgressApply: Boolean
    ) {
        isPlayerModeChangeBtnVisible.value = canUsePlayerMode
        isPlayerRequestBtnVisible.value = showPlayerApplyButton
        hasPlayerApplyRequest.value = hasInProgressApply

        if (!canUsePlayerMode && isPlayerModeFlow.value) {
            dataStoreRepository.putBoolean(DataStoreKey.KEY_IS_MODE_PLAYER, false)
            isPlayerModeFlow.value = false
        }
    }


    fun getImpUid() = viewModelScope.launch {
        impUidString.value = dataStoreRepository.getString(DataStoreKey.KEY_IMP_UID).orEmpty()
        //impUidString.value = "cert_1"+ UUID.randomUUID().toString()
    }

    //임시
    fun setTempImpUid(){
        impUidString.value = "cert_1"+ UUID.randomUUID().toString()
    }

    fun putImpUid(impUid:String) = viewModelScope.launch {
        dataStoreRepository.putString(DataStoreKey.KEY_IMP_UID, impUid)
    }

    fun verifyImpUid(impUid:String, callback: (Int) -> Unit) = viewModelScope.launch {
        val res = playerRepository.verifyImpUid(hashMapOf("imp_uid" to impUid))
        when(res){
            is ResultWrapper.Success ->{
                callback(res.data)
            }
            else -> {}
        }
    }


    fun onModeChange(isPlayerMode: Boolean) {
        isModeChanging.value = true
        isModeChanging.value = false
        /*viewModelScope.launch {
            delay(3000)
            dataStoreRepository.putBoolean(DataStoreKey.KEY_IS_MODE_PLAYER, isPlayerMode)
            isPlayerModeFlow.value = isPlayerMode
            isModeChanging.value = false
        }*/
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object MySetting : Event()
        object ProfileManage : Event()
        object UseHistory : Event()
        object Settle : Event()
        object FavorPlayer : Event()
        object Favor : Event()
        object Notice : Event()
        object FAQ : Event()
        object Term : Event()
        object JoinPlayer : Event()
        object AccompanyCredit : Event()
    }
}
