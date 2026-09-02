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
    val depositorNameEnabled = MutableLiveData(true)

    //스텝3
    val enableStep3Next = MutableLiveData<Boolean>()
    val introduceText = MutableLiveData("")
    val profileImageUri = MutableLiveData<String?>()
    val crcFileUri = MutableLiveData<String?>()
    private var profileImageSavedInCurrentSession = false
    private var criminalRecordSavedInCurrentSession = false
    fun setIntroduceText(text: String) {
        introduceText.value = text
        updateStep3NextButtonState()
    }

    fun setProfileImage(uri: Uri?) {
        profileImageUri.value = uri?.toString()
        profileImageSavedInCurrentSession = false
        updateStep3NextButtonState()
    }

    fun setCrcFile(uri: Uri?) {
        crcFileUri.value = uri?.toString()
        criminalRecordSavedInCurrentSession = false
        updateStep3NextButtonState()
    }

    fun onCrcAgreeChanged(isChecked: Boolean) {
        isCrcAgree.value = isChecked
        updateStep3NextButtonState()
    }
    private fun updateStep3NextButtonState() {
        val imageOk = !profileImageUri.value.isNullOrBlank() || hasStoredProfileImage()
        val introOk = !introduceText.value.isNullOrBlank()
        val agreeOk = isCrcAgree.value == true
        val fileOk = !crcFileUri.value.isNullOrBlank() || hasStoredCriminalRecord()

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
        area2On.value = false
        area2Complete = false
    }

    private fun updateStep1NextButtonState() {
        enableStep1Next.value =
            ageOver.value == true && workAssign.value == true && location.value == true && uniqueInfo.value == true && personalInfo.value == true
    }

    private fun updateStep2NextButtonState() {
        // 기획: 다음 버튼 활성 조건 = 은행 선택 & 계좌번호 숫자 7자리 이상 (예금주는 조건에 포함하지 않음)
        val bankOk = selectedBank.value != null
        val accountDigits = accountNumber.value?.count { it.isDigit() } ?: 0

        enableStep2Next.value = bankOk && accountDigits >= 7
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
        val complete = startArea.value != null &&
            startAreaRadius.value != null &&
            destArea.value != null &&
            destAreaRadius.value != null
        // 코스가 완성되는 시점에 자동으로 ON (동시 ON 허용)
        if (complete && !area1Complete) area1On.value = true
        area1Complete = complete
    }

    fun updateArea2On(){
        val complete = startArea2.value != null &&
            startAreaRadius2.value != null &&
            destArea2.value != null &&
            destAreaRadius2.value != null
        if (complete && !area2Complete) area2On.value = true
        area2Complete = complete
    }

    private var area1Complete = false
    private var area2Complete = false

    // 여러 코스를 동시에 ON 할 수 있고, 최소 1개는 ON을 유지해야 한다
    fun onAreaOnToggle(index: Int) {
        val current = if (index == 1) area1On.value else area2On.value
        if (current) {
            val onCount = (if (area1On.value) 1 else 0) +
                (if (addArea.value == true && area2On.value) 1 else 0)
            if (onCount <= 1) {
                _event.value = Event.ShowMessage("매일 동행 코스는 최소 1개를 ON으로 유지해야 해요.")
                return
            }
        }
        if (index == 1) area1On.value = !current else area2On.value = !current
    }



    val playerApplyedInfo = MutableStateFlow<PlayerApplyedInfoDto?>(null)
    fun getPlayers(isResume: Boolean = false) = viewModelScope.launch {
        val res = playerRepository.getPlayers()
        when(res){
            is ResultWrapper.Success ->{
                playerApplyedInfo.value = res.data
                restoreApplication(res.data)
                if (isResume) {
                    onClickStepNext(res.data.resumeStep())
                }
            }
            else -> {}
        }
    }

    fun onClickStep2Next() {
        if (enableStep2Next.value == true) {
            _event.value = Event.CheckBankAccount
        }
    }

    private fun restoreApplication(info: PlayerApplyedInfoDto) {
        restoreTerms(info.terms)
        restoreBank(info)
        restoreProfile(info)
        restoreAreas(info.areas)
    }

    private fun restoreTerms(terms: List<String>) {
        if (terms.isEmpty()) return

        allAgreeStep1.value = true
        ageOver.value = true
        workAssign.value = true
        location.value = true
        uniqueInfo.value = true
        personalInfo.value = true
        updateStep1NextButtonState()
    }

    private fun restoreBank(info: PlayerApplyedInfoDto) {
        val bank = info.bank ?: run {
            depositorNameEnabled.value = true
            return
        }
        val bankCd = bank.bank_cd.orEmpty()
        selectedBank.value = bankList.value.firstOrNull { it.code == bankCd }
            ?: BaseDto(
                code = bankCd,
                cate = "",
                name = bank.bank_name.orEmpty(),
                description = ""
            )
        accountNumber.value = bank.account_number.orEmpty()
        depositorName.value = bank.account_depositor.orEmpty()
        depositorNameEnabled.value = !(info.verify_phone && !bank.account_depositor.isNullOrBlank())
        updateStep2NextButtonState()
    }

    private fun restoreProfile(info: PlayerApplyedInfoDto) {
        if (!info.profile_image.isNullOrBlank()) {
            profileImageUri.value = info.profile_image
        }
        if (!info.introduction.isNullOrBlank()) {
            introduceText.value = info.introduction
        }
        if (!info.criminalrecord_file_name.isNullOrBlank()) {
            crcFileUri.value = info.criminalrecord_file_name
            documentFileName.value = info.criminalrecord_file_name
            isCrcAgree.value = true
        }
        updateStep3NextButtonState()
    }

    private fun restoreAreas(areas: List<AreaDto>) {
        // OFF 코스도 복원한다 (사용자가 끈 상태 그대로)
        restorePrimaryArea(areas.getOrNull(0))
        restoreSecondaryArea(areas.getOrNull(1))
    }

    private fun PlayerApplyedInfoDto.resumeStep(): Int {
        return when {
            !certi_req_date.isNullOrBlank() -> 4
            !verify_bank -> 1
            profile_image.isNullOrBlank() || introduction.isNullOrBlank() || criminalrecord_file_name.isNullOrBlank() -> 3
            areas.isEmpty() -> 4
            else -> 4
        }
    }

    private fun restorePrimaryArea(area: AreaDto?) {
        if (area == null) return

        areaId1.value = area.player_area_id
        startArea.value = area.toDepartSearchModel()
        startAreaRadius.value = area.depart_range
        destArea.value = area.toDestSearchModel()
        destAreaRadius.value = area.dest_range
        area1Changed.value = false
        area1Complete = true
        area1On.value = area.use_yn == "Y"
        updateAddAreaBtn()
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
        area2Complete = true
        area2On.value = area.use_yn == "Y"
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


    fun submitPlayerProfile(playerId: Int, result: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            val pendingFiles = listOfNotNull(profileFile.value, criminalFile.value)

            if (getApplyValidationMessage() != null) {
                result(false, getApplyValidationMessage())
                return@launch
            }

            val profileFile = profileFile.value
            val criminalFile = criminalFile.value

            loadingState.value = true

            try {
                if (profileFile != null) {
                    val profileResult = playerRepository.postProfileImage(
                        playerId = playerId,
                        file = createFilePart(partName = "file", file = profileFile)
                    )
                    if (profileResult !is ResultWrapper.Success) {
                        loadingState.value = false
                        result(false, profileResult.errorMessage("프로필 사진 저장에 실패했습니다."))
                        return@launch
                    }
                    profileImageSavedInCurrentSession = true
                }

                val introduceResult = playerRepository.postIntroduction(
                    playerId = playerId,
                    request = hashMapOf("introduction" to introduceText.value.orEmpty())
                )
                if (introduceResult !is ResultWrapper.Success) {
                    loadingState.value = false
                    result(false, introduceResult.errorMessage("자기소개 저장에 실패했습니다."))
                    return@launch
                }

                if (criminalFile != null) {
                    val criminalResult = playerRepository.postCriminalRecord(
                        playerId = playerId,
                        file = createFilePart(partName = "file", file = criminalFile)
                    )
                    if (criminalResult !is ResultWrapper.Success) {
                        loadingState.value = false
                        result(false, criminalResult.errorMessage("범죄경력회보서 저장에 실패했습니다."))
                        return@launch
                    }
                    criminalRecordSavedInCurrentSession = true
                }

                loadingState.value = false
                cleanupPendingFiles(pendingFiles)
                result(true, null)
            } catch (e: Exception) {
                loadingState.value = false
                Timber.e(e, "Failed to submit player profile")
                result(false, "플레이어 정보를 저장하지 못했습니다.")
            }
        }

    fun requestPlayerApply(playerId: Int, result: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            val validationMessage = getApplyValidationMessage()
            if (validationMessage != null) {
                result(false, validationMessage)
                return@launch
            }

            loadingState.value = true

            try {
                if (!savePrimaryArea(playerId)) {
                    loadingState.value = false
                    result(false, "동행범위1 저장에 실패했습니다.")
                    return@launch
                }

                if (!saveSecondaryAreaIfNeeded(playerId)) {
                    loadingState.value = false
                    result(false, "동행범위2 저장에 실패했습니다.")
                    return@launch
                }

                when (val applyResult = playerRepository.postPlayerApply(playerId)) {
                    is ResultWrapper.Success -> {
                        loadingState.value = false
                        result(true, null)
                    }
                    else -> {
                        loadingState.value = false
                        result(false, applyResult.errorMessage("플레이어 신청 제출에 실패했습니다."))
                    }
                }

            } catch (e: Exception) {
                loadingState.value = false
                Timber.e(e, "Failed to request player apply")
                result(false, "플레이어 신청 제출에 실패했습니다.")
            }
        }

    private fun cleanupPendingFiles(files: List<File>) {
        files.forEach { it.delete() }
        profileFile.value = null
        criminalFile.value = null
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
            profileFile.value == null && !hasStoredProfileImage() -> "프로필 사진을 등록해 주세요."
            introduceText.value.isNullOrBlank() -> "자기소개를 입력해 주세요."
            criminalFile.value == null && !hasStoredCriminalRecord() -> "범죄경력회보서를 등록해 주세요."
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
        enable: Boolean = true,
        start: KakaoSearchModel,
        startRadius: Int,
        dest: KakaoSearchModel,
        destRadius: Int
    ): PlayerAreaAddRequest =
        PlayerAreaAddRequest(
            area_id = areaId,
            is_domestic = true,
            enable = enable,
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
        // iOS와 동일: area_id가 있으면 수정(417), 삭제 후 재등록하지 않는다. enable도 함께 전송.
        val areaResult = playerRepository.postPlayerArea(
            playerId = playerId,
            request = createAreaRequest(
                areaId = areaId1.value,
                enable = area1On.value,
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

        val areaResult = playerRepository.postPlayerArea(
            playerId = playerId,
            request = createAreaRequest(
                areaId = areaId2.value,
                enable = area2On.value,
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

    private fun hasStoredProfileImage(): Boolean =
        profileImageSavedInCurrentSession || !playerApplyedInfo.value?.profile_image.isNullOrBlank()

    private fun hasStoredCriminalRecord(): Boolean =
        criminalRecordSavedInCurrentSession || !playerApplyedInfo.value?.criminalrecord_file_name.isNullOrBlank()

    private fun ResultWrapper<*>.errorMessage(defaultMessage: String): String =
        when (this) {
            is ResultWrapper.GenericError -> message?.takeIf { it.isNotBlank() } ?: defaultMessage
            is ResultWrapper.NetworkError -> "네트워크 상태를 확인해 주세요."
            is ResultWrapper.Success -> defaultMessage
        }

    private fun ResultWrapper<*>.isAreaLimitExceeded(): Boolean =
        this is ResultWrapper.GenericError &&
            code == "400" &&
            errorData?.type == "LimitExceeded" &&
            errorData.message?.contains("배송가능지역") == true

    fun postPlayerBank(
        playerId: Int,
        requestBank: BankRequestDto,
        result: (Boolean, String?) -> Unit
    ) = viewModelScope.launch {
        loadingState.value = true
        val res = playerRepository.postPlayerBank(
            playerId = playerId,
            request = requestBank
        )
        when(res){
            is ResultWrapper.Success ->{
                loadingState.value = false
                result(true, null)
            }
            is ResultWrapper.GenericError -> {
                loadingState.value = false
                result(false, res.message)
            }
            else -> {
                loadingState.value = false
                result(false, null)
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

    fun onTermDetailClick(index: Int) {
        val fallback = fallbackPlayerTerms.getOrNull(index) ?: return
        val term = findPlayerTerm(index, fallback)
        val title = term?.name?.takeIf { it.isNotBlank() } ?: fallback.title
        val content = term?.description?.takeIf { it.isNotBlank() } ?: fallback.content
        _event.value = Event.TermDetail(title, content)
    }

    private fun findPlayerTerm(index: Int, fallback: FallbackPlayerTerm): TermMeta? {
        val termList = terms.value.orEmpty()
        return termList.firstOrNull { term ->
            fallback.keywords.any { keyword ->
                term.name.contains(keyword) || term.code.contains(keyword, ignoreCase = true)
            }
        } ?: termList.getOrNull(index)
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

    fun cancelPlayerApplication(result: (Boolean, String?) -> Unit) = viewModelScope.launch {
        val playerId = playerApplyedInfo.value?.player_id
        if (playerId == null) {
            result(true, null)
            return@launch
        }

        loadingState.value = true
        when (val res = playerRepository.cancelPlayerApplication(playerId)) {
            is ResultWrapper.Success -> {
                loadingState.value = false
                cleanupPendingFiles(listOfNotNull(profileFile.value, criminalFile.value))
                result(true, null)
            }
            is ResultWrapper.GenericError -> {
                loadingState.value = false
                result(false, res.message)
            }
            is ResultWrapper.NetworkError -> {
                loadingState.value = false
                result(false, null)
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
        object Cancel : Event()
        object AttachImage : Event()
        object AccountNumber : Event()
        object Bank : Event()
        object CheckBankAccount : Event()

        object ProfileNext : Event()
        object Complete : Event()
        object StartArea : Event()
        object DestinaitonArea : Event()

        object StartArea2 : Event()
        object DestinaitonArea2 : Event()

        data class ShowMessage(val message: String) : Event()
        data class TermDetail(val title: String, val content: String) : Event()
    }

    private data class FallbackPlayerTerm(
        val title: String,
        val content: String,
        val keywords: List<String>,
    )

    companion object {
        private val fallbackPlayerTerms = listOf(
            FallbackPlayerTerm(
                title = "만 19세 이상입니다.",
                content = "만 19세 이상임에 동의합니다.",
                keywords = listOf("19", "성인", "age")
            ),
            FallbackPlayerTerm(
                title = "업무위수탁약관",
                content = "업무위수탁약관",
                keywords = listOf("업무", "위수탁", "business")
            ),
            FallbackPlayerTerm(
                title = "위치정보 이용 동의",
                content = "위치정보 이용 동의",
                keywords = listOf("위치", "location")
            ),
            FallbackPlayerTerm(
                title = "고유식별정보 수집 및 이용 동의",
                content = "고유식별정보 수집 및 이용 동의",
                keywords = listOf("고유", "식별", "unique")
            ),
            FallbackPlayerTerm(
                title = "개인 정보의 제3자 제공에 대한 동의",
                content = "개인 정보의 제3자 제공에 대한 동의",
                keywords = listOf("개인", "제3자", "personal")
            )
        )
    }
}
