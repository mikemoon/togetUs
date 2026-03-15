package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerApplyedInfoDto
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartViewModel.Event
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.uuid.Uuid

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
    fun getMyProfile(result: (ProfileDto) -> Unit) = viewModelScope.launch {
        val profileData = dataStoreRepository.getProfile(DataStoreKey.KEY_PROFILE)
        if (profileData != null) {
            Timber.d("profileAlreadyHas profileData $profileData")
            result.invoke(profileData)
            profileDto.value = profileData
        } else {
            when (val response = userRepository.getMyProfile()) {
                is ResultWrapper.Success -> {
                    dataStoreRepository.putProfile(DataStoreKey.KEY_PROFILE, response.data)
                    profileDto.value = response.data
                    result.invoke(response.data)
                }

                else -> {}
            }
        }
    }

    val hasPlayerApplyRequest = MutableStateFlow(false)
    val playerApplyedInfoDto = MutableStateFlow<PlayerApplyedInfoDto?>(null)
    fun getPlayerInfo() = viewModelScope.launch {
        val res = playerRepository.getPlayers()
        when(res){
            is ResultWrapper.Success ->{
                val dto = res.data
                isPlayerRequestBtnVisible.value = dto.certi_res_date.isNullOrEmpty()
                isPlayerModeChangeBtnVisible.value = !isPlayerRequestBtnVisible.value
                playerApplyedInfoDto.value = res.data
            }
            else -> {
                hasPlayerApplyRequest.value = false
            }
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