package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseDto
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.data.remote.dto.player.AreaDto
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerApplyedInfoDto
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaLocationRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.utils.FileUtil.createFilePart
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerJoinViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository,
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val step = MutableLiveData<Int>(1)
    val stepTitle = MutableStateFlow("약관 동의")

    val enableStep1Next = MutableLiveData<Boolean>()

    //스텝2
    val enableStep2Next = MutableLiveData<Boolean>()
    val accountNumber = MutableLiveData("")
    val depositorName = MutableLiveData("")

    //스텝3
    val enableStep3Next = MutableLiveData<Boolean>()
    val introduceText = MutableLiveData("")
    val profileImageUri = MutableLiveData<String?>()
    val crcFileUri = MutableLiveData<String?>()
    fun setIntroduceText(text: String) {
        introduceText.value = text
        updateStep3NextButtonState()
    }

    fun setProfileImage(uri: Uri?) {
        profileImageUri.value = uri?.toString()
        updateStep3NextButtonState()
    }

    fun setCrcFile(uri: Uri?) {
        crcFileUri.value = uri?.toString()
        updateStep3NextButtonState()
    }

    fun onCrcAgreeChanged(isChecked: Boolean) {
        isCrcAgree.value = isChecked
        updateStep3NextButtonState()
    }
    private fun updateStep3NextButtonState() {
        val imageOk = !profileImageUri.value.isNullOrBlank()
        val introOk = !introduceText.value.isNullOrBlank()
        val agreeOk = isCrcAgree.value == true
        val fileOk = !crcFileUri.value.isNullOrBlank()

        enableStep3Next.value = imageOk && introOk && agreeOk && fileOk
    }



    val allAgreeStep1 = MutableLiveData<Boolean>(false)

    init {
        getBanks()
        getPlayerTerms()
    }

    fun onAllAgreeClick(isAgree: Boolean) {
        allAgreeStep1.value = isAgree
        ageOver.value = isAgree
        workAssign.value = isAgree
        location.value = isAgree
        uniqueInfo.value = isAgree
        personalInfo.value = isAgree
        updateStep1NextButtonState()
    }

    val bankList = MutableStateFlow<List<BaseDto>>(emptyList())
    fun getBanks() = viewModelScope.launch {
        val res = configRepository.getBanks()
        when(res){
            is ResultWrapper.Success ->{
                bankList.value = res.data
            }
            else -> {}
        }
    }
    val selectedBank = MutableStateFlow<BaseDto?>(null)

    fun setSelectedBank(bank: BaseDto) {
        selectedBank.value = bank
        updateStep2NextButtonState()
    }

    //계좌번호
    fun setAccountNumber(text: String) {
        accountNumber.value = text
        updateStep2NextButtonState()
    }

    //예금주
    fun setDepositorName(text: String) {
        depositorName.value = text
        updateStep2NextButtonState()
    }

    //범죄회부서 동의
    val isCrcAgree = MutableLiveData(false)


    fun postPlayer() = viewModelScope.launch {
        playerRepository.postPlayer(hashMapOf())
    }

    val ageOver = MutableLiveData<Boolean>(false)
    fun onAgeOverClick(isAgree: Boolean) {
        ageOver.value = isAgree
        updateStep1NextButtonState()
    }

    val workAssign = MutableLiveData<Boolean>(false)
    fun onWorkAssignClick(isAgree: Boolean) {
        workAssign.value = isAgree
        updateStep1NextButtonState()
    }

    val location = MutableLiveData<Boolean>(false)
    fun onLocationClick(isAgree: Boolean) {
        location.value = isAgree
        updateStep1NextButtonState()
    }

    val uniqueInfo = MutableLiveData<Boolean>(false)
    fun onUniqueInfoClick(isAgree: Boolean) {
        uniqueInfo.value = isAgree
        updateStep1NextButtonState()
    }

    val personalInfo = MutableLiveData<Boolean>(false)
    fun onPersonalInfoClick(isAgree: Boolean) {
        personalInfo.value = isAgree
        updateStep1NextButtonState()
    }

    fun onClickStepNext(step: Int) {
        this.step.value = step
        stepTitle.value = when(step){
            1 -> "약관 동의"
            2 -> "계좌 정보 입력"
            3 -> "플레이어 정보 입력"
            4 -> "동행지 설정"
            else -> "약관 동의"
        }
    }

    val deleteArea = MutableStateFlow<Boolean>(false)
    fun onDeleteArea(isDelete: Boolean){
        deleteArea.value = isDelete
    }

    val area1On = MutableStateFlow(false)
    val area2On = MutableStateFlow(false)

    val addAreaBtnEnable = MutableLiveData<Boolean>(false)
    val addArea = MutableLiveData<Boolean>(false)
    fun onAddArea(isAdd: Boolean){
        addArea.value = isAdd
    }

    fun clearSecondaryArea() {
        addArea.value = false
        deleteArea.value = false
        areaId2.value = null
        area2Changed.value = false
        startArea2.value = null
        startAreaRadius2.value = null
        destArea2.value = null
        destAreaRadius2.value = null
        updateArea2On()
    }

    private fun updateStep1NextButtonState() {
        enableStep1Next.value =
            ageOver.value == true && workAssign.value == true && location.value == true && uniqueInfo.value == true && personalInfo.value == true
    }

    private fun updateStep2NextButtonState() {
        val bankOk = selectedBank.value != null
        val accountOk = !accountNumber.value.isNullOrBlank()
        val nameOk = !depositorName.value.isNullOrBlank()

        enableStep2Next.value = bankOk && accountOk && nameOk
    }


    val startArea = MutableStateFlow<KakaoSearchModel?>(null)
    val startAreaRadius = MutableStateFlow<Int?>(null)
    val destArea = MutableStateFlow<KakaoSearchModel?>(null)
    val destAreaRadius = MutableStateFlow<Int?>(null)


    val startArea2 = MutableStateFlow<KakaoSearchModel?>(null)
    val startAreaRadius2 = MutableStateFlow<Int?>(null)
    val destArea2 = MutableStateFlow<KakaoSearchModel?>(null)
    val destAreaRadius2 = MutableStateFlow<Int?>(null)
    private val areaId1 = MutableStateFlow<Int?>(null)
    private val areaId2 = MutableStateFlow<Int?>(null)
    private val area1Changed = MutableStateFlow(false)
    private val area2Changed = MutableStateFlow(false)

    fun setStartArea(model: KakaoSearchModel) {
        startArea.value = model
        area1Changed.value = true
        updateAddAreaBtn()
        updateArea1On()
    }

    fun setStartAreaRadius(radius: Int){
        startAreaRadius.value = radius
        area1Changed.value = true
        updateAddAreaBtn()
        updateArea1On()
    }

    fun setStartArea2(model: KakaoSearchModel) {
        startArea2.value = model
        area2Changed.value = true
        updateArea2On()
    }

    fun setStartAreaRadius2(radius: Int){
        startAreaRadius2.value = radius
        area2Changed.value = true
        updateArea2On()
    }

    fun setDestArea(model: KakaoSearchModel) {
        destArea.value = model
        area1Changed.value = true
        updateAddAreaBtn()
        updateArea1On()
    }

    fun setDestAreaRadius(radius: Int){
        destAreaRadius.value = radius
        area1Changed.value = true
        updateAddAreaBtn()
        updateArea1On()
    }

    fun setDestArea2(model: KakaoSearchModel) {
        destArea2.value = model
        area2Changed.value = true
        updateArea2On()
    }

    fun setDestAreaRadius2(radius: Int){
        destAreaRadius2.value = radius
        area2Changed.value = true
        updateArea2On()
    }

    fun updateAddAreaBtn(){
        addAreaBtnEnable.value = (
            startArea.value != null &&
                startAreaRadius.value != null &&
                destArea.value != null &&
                destAreaRadius.value != null
            )
    }

    fun updateArea1On(){
        area1On.value = (
            startArea.value != null &&
                startAreaRadius.value != null &&
                destArea.value != null &&
                destAreaRadius.value != null
            )
    }

    fun updateArea2On(){
        area2On.value = (
            startArea2.value != null &&
                startAreaRadius2.value != null &&
                destArea2.value != null &&
                destAreaRadius2.value != null
            )
    }



    val playerApplyedInfo = MutableStateFlow<PlayerApplyedInfoDto?>(null)
    fun getPlayers() = viewModelScope.launch {
        val res = playerRepository.getPlayers()
        when(res){
            is ResultWrapper.Success ->{
                playerApplyedInfo.value = res.data
                restoreAreas(res.data.areas)
            }
            else -> {}
        }
    }

    private fun restoreAreas(areas: List<AreaDto>) {
        val enabledAreas = areas.filter { it.use_yn != "N" }
        restorePrimaryArea(enabledAreas.getOrNull(0))
        restoreSecondaryArea(enabledAreas.getOrNull(1))
    }

    private fun restorePrimaryArea(area: AreaDto?) {
        if (area == null) return

        areaId1.value = area.player_area_id
        startArea.value = area.toDepartSearchModel()
        startAreaRadius.value = area.depart_range
        destArea.value = area.toDestSearchModel()
        destAreaRadius.value = area.dest_range
        area1Changed.value = false
        updateAddAreaBtn()
        updateArea1On()
    }

    private fun restoreSecondaryArea(area: AreaDto?) {
        if (area == null) return

        addArea.value = true
        areaId2.value = area.player_area_id
        startArea2.value = area.toDepartSearchModel()
        startAreaRadius2.value = area.depart_range
        destArea2.value = area.toDestSearchModel()
        destAreaRadius2.value = area.dest_range
        area2Changed.value = false
        updateArea2On()
    }

    private fun AreaDto.toDepartSearchModel(): KakaoSearchModel =
        KakaoSearchModel(
            name = depart_address.orEmpty(),
            lat = depart_latitude,
            lng = depart_longitude,
            subtitle = depart_address2,
            distance = null,
            roadAddress = depart_address2,
            source = "SAVED"
        )

    private fun AreaDto.toDestSearchModel(): KakaoSearchModel =
        KakaoSearchModel(
            name = dest_address.orEmpty(),
            lat = dest_latitude,
            lng = dest_longitude,
            subtitle = dest_address2,
            distance = null,
            roadAddress = dest_address2,
            source = "SAVED"
        )

    val profileFile = MutableLiveData<File?>()
    val criminalFile = MutableLiveData<File?>()
    val documentFileName = MutableStateFlow("")


    fun requestPlayerApply(playerId: Int, result: (Boolean) -> Unit) =
        viewModelScope.launch {

            if (getApplyValidationMessage() != null) {
                result(false)
                return@launch
            }

            val start = startArea.value ?: return@launch
            val dest = destArea.value ?: return@launch
            val bank = selectedBank.value ?: return@launch

            val profileFile = profileFile.value ?: return@launch
            val criminalFile = criminalFile.value ?: return@launch

            loadingState.value = true

            try {

                val bankResult = playerRepository.postPlayerBank(
                    playerId = playerId,
                    request = BankRequestDto(
                        term_cds = terms.value?.map { it.code } ?: listOf("terms_1", "terms_2", "terms_3"),
                        account_number = accountNumber.value.orEmpty(),
                        account_depositor = depositorName.value.orEmpty(),
                        bank_cd = bank.code
                    )
                )
                if (bankResult !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }

                val profileResult = playerRepository.postProfileImage(
                    playerId = playerId,
                    file = createFilePart(partName = "file", file = profileFile)
                )
                if (profileResult !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }

                val introduceResult = playerRepository.postIntroduction(
                    playerId = playerId,
                    request = hashMapOf("introduction" to introduceText.value.orEmpty())
                )
                if (introduceResult !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }

                val criminalResult = playerRepository.postCriminalRecord(
                    playerId = playerId,
                    file = createFilePart(partName = "file", file = criminalFile)
                )
                if (criminalResult !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }

                if (!savePrimaryArea(playerId)) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }

                if (!saveSecondaryAreaIfNeeded(playerId)) {
                    loadingState.value = false
                    result(false)
                    return@launch
                }

                val applyResult = playerRepository.postPlayerApply(playerId)
                loadingState.value = false
                result(applyResult is ResultWrapper.Success)

            } catch (e: Exception) {
                loadingState.value = false
                result(false)
            }
        }

    fun saveSecondaryAreaIfReady(result: (Boolean) -> Unit) = viewModelScope.launch {
        val playerId = playerApplyedInfo.value?.player_id
        if (playerId == null || startArea2.value == null || destArea2.value == null) {
            result(false)
            return@launch
        }

        loadingState.value = true
        val success = saveSecondaryAreaIfNeeded(playerId)
        loadingState.value = false
        result(success)
    }

    fun deleteSecondaryArea(result: (Boolean) -> Unit) = viewModelScope.launch {
        val playerId = playerApplyedInfo.value?.player_id
        val areaId = areaId2.value
        if (areaId == null || playerId == null) {
            clearSecondaryArea()
            result(true)
            return@launch
        }

        loadingState.value = true
        when (playerRepository.deletePlayerArea(playerId, areaId)) {
            is ResultWrapper.Success -> {
                loadingState.value = false
                clearSecondaryArea()
                result(true)
            }
            else -> {
                loadingState.value = false
                result(false)
            }
        }
    }

    fun getApplyValidationMessage(): String? {
        val secondaryAreaEnabled = addArea.value == true

        return when {
            selectedBank.value == null -> "은행을 선택해 주세요."
            accountNumber.value.isNullOrBlank() -> "계좌번호를 입력해 주세요."
            depositorName.value.isNullOrBlank() -> "예금주를 입력해 주세요."
            profileFile.value == null -> "프로필 사진을 등록해 주세요."
            introduceText.value.isNullOrBlank() -> "자기소개를 입력해 주세요."
            criminalFile.value == null -> "범죄경력회보서를 등록해 주세요."
            startArea.value == null -> "픽업 가능 지역을 선택해 주세요."
            startAreaRadius.value == null -> "픽업 가능 반경을 선택해 주세요."
            startArea.value?.lat == null || startArea.value?.lng == null -> "픽업 가능 지역을 다시 선택해 주세요."
            destArea.value == null -> "도착 가능 지역을 선택해 주세요."
            destAreaRadius.value == null -> "도착 가능 반경을 선택해 주세요."
            destArea.value?.lat == null || destArea.value?.lng == null -> "도착 가능 지역을 다시 선택해 주세요."
            secondaryAreaEnabled && startArea2.value == null -> "동행범위2 픽업 가능 지역을 선택해 주세요."
            secondaryAreaEnabled && startAreaRadius2.value == null -> "동행범위2 픽업 가능 반경을 선택해 주세요."
            secondaryAreaEnabled && (startArea2.value?.lat == null || startArea2.value?.lng == null) -> "동행범위2 픽업 가능 지역을 다시 선택해 주세요."
            secondaryAreaEnabled && destArea2.value == null -> "동행범위2 도착 가능 지역을 선택해 주세요."
            secondaryAreaEnabled && destAreaRadius2.value == null -> "동행범위2 도착 가능 반경을 선택해 주세요."
            secondaryAreaEnabled && (destArea2.value?.lat == null || destArea2.value?.lng == null) -> "동행범위2 도착 가능 지역을 다시 선택해 주세요."
            else -> null
        }
    }

    private fun createAreaRequest(
        areaId: Int? = null,
        start: KakaoSearchModel,
        startRadius: Int,
        dest: KakaoSearchModel,
        destRadius: Int
    ): PlayerAreaAddRequest =
        PlayerAreaAddRequest(
            area_id = areaId,
            is_domestic = true,
            enable = true,
            depart = PlayerAreaLocationRequest(
                address = start.name,
                address2 = start.roadAddress.orEmpty(),
                latitude = start.lat ?: 0.0,
                longitude = start.lng ?: 0.0,
                range = startRadius
            ),
            dest = PlayerAreaLocationRequest(
                address = dest.name,
                address2 = dest.roadAddress.orEmpty(),
                latitude = dest.lat ?: 0.0,
                longitude = dest.lng ?: 0.0,
                range = destRadius
            )
        )

    private suspend fun savePrimaryArea(playerId: Int): Boolean {
        val currentAreaId = areaId1.value
        if (currentAreaId != null && !area1Changed.value) return true

        if (currentAreaId != null && !deleteArea(playerId, currentAreaId)) return false

        val areaResult = playerRepository.postPlayerArea(
            playerId = playerId,
            request = createAreaRequest(
                start = startArea.value ?: return false,
                startRadius = startAreaRadius.value ?: return false,
                dest = destArea.value ?: return false,
                destRadius = destAreaRadius.value ?: return false
            )
        )
        return when (areaResult) {
            is ResultWrapper.Success -> {
                areaId1.value = areaResult.data
                area1Changed.value = false
                true
            }
            else -> areaResult.isAreaLimitExceeded()
        }
    }

    private suspend fun saveSecondaryAreaIfNeeded(playerId: Int): Boolean {
        if (addArea.value != true || startArea2.value == null || destArea2.value == null) return true

        val currentAreaId = areaId2.value
        if (currentAreaId != null && !area2Changed.value) return true

        if (currentAreaId != null && !deleteArea(playerId, currentAreaId)) return false

        val areaResult = playerRepository.postPlayerArea(
            playerId = playerId,
            request = createAreaRequest(
                start = startArea2.value ?: return false,
                startRadius = startAreaRadius2.value ?: return false,
                dest = destArea2.value ?: return false,
                destRadius = destAreaRadius2.value ?: return false
            )
        )
        return when (areaResult) {
            is ResultWrapper.Success -> {
                areaId2.value = areaResult.data
                area2Changed.value = false
                true
            }
            else -> areaResult.isAreaLimitExceeded()
        }
    }

    private suspend fun deleteArea(playerId: Int, areaId: Int): Boolean =
        playerRepository.deletePlayerArea(playerId, areaId) is ResultWrapper.Success

    private fun ResultWrapper<*>.isAreaLimitExceeded(): Boolean =
        this is ResultWrapper.GenericError &&
            code == "400" &&
            errorData?.type == "LimitExceeded" &&
            errorData.message?.contains("배송가능지역") == true

    fun postPlayerBank(playerId: Int,
                       requestBank: BankRequestDto,
                       result: (Boolean) -> Unit) = viewModelScope.launch {
                           loadingState.value = true
        val res = playerRepository.postPlayerBank(
            playerId = playerId,
            request = requestBank
        )
        when(res){
            is ResultWrapper.Success ->{
                result(true)
                loadingState.value = false
            }
            else -> {
                result(false)
                loadingState.value = false
            }
        }
    }


    val terms = MutableStateFlow<List<TermMeta>?>(null)
    fun getPlayerTerms() = viewModelScope.launch {
        val res = configRepository.getPlayerTerms()
        when(res){
            is ResultWrapper.Success ->{
                terms.value = res.data
            }
            else -> {}
        }
    }

    fun postPlayerProfileImage(playerId: Int, file: MultipartBody.Part, result: (Boolean) -> Unit) = viewModelScope.launch {
        val res = playerRepository.postProfileImage(playerId, file)
        when(res){
            is ResultWrapper.Success ->{
                result(true)
            }
            else -> {
                result(false)
            }
        }
    }

    fun postPlayerIntroduce(playerId: Int, introduceText: String) = viewModelScope.launch {
        val res = playerRepository.postIntroduction(playerId, hashMapOf("introduction" to introduceText))
        when(res){
            is ResultWrapper.Success ->{

            }
            else -> {

            }
        }
    }

    fun postPlayerCriminalRecord(playerId: Int, file: MultipartBody.Part) = viewModelScope.launch {
        val res = playerRepository.postCriminalRecord(playerId, file)
        when(res){
            is ResultWrapper.Success ->{

            }
            else -> {

            }
        }
    }


    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object AttachImage : Event()
        object AccountNumber : Event()
        object Bank : Event()

        object Complete : Event()
        object StartArea : Event()
        object DestinaitonArea : Event()

        object StartArea2 : Event()
        object DestinaitonArea2 : Event()
    }
}
