package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseDto
import sky.kr.co.newtogetusa.data.remote.dto.auth.TermMeta
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerApplyedInfoDto
import sky.kr.co.newtogetusa.data.remote.request.player.AreaRequest
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequest
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.data.remote.request.player.CriminalRequest
import sky.kr.co.newtogetusa.data.remote.request.player.LocationRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerJoinMultipartData
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerJoinRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.data.remote.request.player.ProfileImageRequest
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

    fun setStartArea(model: KakaoSearchModel) {
        startArea.value = model
        updateAddAreaBtn()
        updateArea1On()
    }

    fun setStartAreaRadius(radius: Int){
        startAreaRadius.value = radius
    }

    fun setStartArea2(model: KakaoSearchModel) {
        startArea2.value = model
        updateArea2On()
    }

    fun setStartAreaRadius2(radius: Int){
        startAreaRadius2.value = radius
    }

    fun setDestArea(model: KakaoSearchModel) {
        destArea.value = model
        updateAddAreaBtn()
        updateArea1On()
    }

    fun setDestAreaRadius(radius: Int){
        destAreaRadius.value = radius
    }

    fun setDestArea2(model: KakaoSearchModel) {
        destArea2.value = model
        updateArea2On()
    }

    fun setDestAreaRadius2(radius: Int){
        destAreaRadius2.value = radius
    }

    fun updateAddAreaBtn(){
        addAreaBtnEnable.value = (startArea.value != null && destArea.value != null)
    }

    fun updateArea1On(){
        area1On.value = (startArea.value != null && destArea.value != null)
    }

    fun updateArea2On(){
        area2On.value = (startArea2.value != null && destArea2.value != null)
    }



    val playerApplyedInfo = MutableStateFlow<PlayerApplyedInfoDto?>(null)
    fun getPlayers() = viewModelScope.launch {
        val res = playerRepository.getPlayers()
        when(res){
            is ResultWrapper.Success ->{
                playerApplyedInfo.value = res.data
            }
            else -> {}
        }
    }

    val profileFile = MutableLiveData<File?>()
    val criminalFile = MutableLiveData<File?>()
    val documentFileName = MutableStateFlow("")


    fun requestPlayerApply(playerId: Int, result: (Boolean) -> Unit) =
        viewModelScope.launch {

            val start = startArea.value ?: return@launch
            val dest = destArea.value ?: return@launch
            val bank = selectedBank.value ?: return@launch

            val profileFile = profileFile.value ?: return@launch
            val criminalFile = criminalFile.value ?: return@launch

            loadingState.value = true

            try {

                // 1️⃣ JSON 데이터 생성 (파일 제외)
                val jsonData = PlayerJoinMultipartData(
                    bank = BankRequest(
                        term_cds = listOf("terms_1", "terms_2", "terms_3"),
                        bank_cd = bank.code,
                        account_number = accountNumber.value.orEmpty(),
                        account_depositor = depositorName.value.orEmpty()
                    ),
                    introduction = introduceText.value.orEmpty(),
                    areas = listOf(
                        AreaRequest(
                            depart = LocationRequest(
                                address = start.name,
                                address2 = start.roadAddress.orEmpty(),
                                latitude = start.lat ?: return@launch,
                                longitude = start.lng ?: return@launch,
                                range = startAreaRadius.value ?: return@launch
                            ),
                            dest = LocationRequest(
                                address = dest.name,
                                address2 = dest.roadAddress.orEmpty(),
                                latitude = dest.lat ?: return@launch,
                                longitude = dest.lng ?: return@launch,
                                range = destAreaRadius.value ?: return@launch
                            )
                        )
                    )
                )

                val gson = Gson()
                val jsonString = gson.toJson(jsonData)

                val dataRequestBody =
                    jsonString.toRequestBody("application/json".toMediaType())

                // 2️⃣ 파일 Multipart 변환
                val profilePart = createFilePart(
                    partName = "profile_image",
                    file = profileFile
                )

                val criminalPart = createFilePart(
                    partName = "criminal_record",
                    file = criminalFile
                )

                // 3️⃣ API 호출
                when (val res =
                    playerRepository.postPlayerApplyBatch(
                        playerId,
                        dataRequestBody,
                        profilePart,
                        criminalPart
                    )
                ) {

                    is ResultWrapper.Success -> {
                        loadingState.value = false
                        result(true)
                    }

                    else -> {
                        loadingState.value = false
                        result(false)
                    }
                }

            } catch (e: Exception) {
                loadingState.value = false
                result(false)
            }
        }

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