package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.databinding.FragmentJoinPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomAccountInfoDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomPictureTypeDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.PictureType
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.FileUtil.uriToFile
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.hideKeyboard
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PlayerJoinFragment : BaseFragment<FragmentJoinPlayerBinding, PlayerJoinViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_join_player
    override val viewModel: PlayerJoinViewModel by viewModels()
    private val args: PlayerJoinFragmentArgs by navArgs()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var currentPhotoUri: Uri

    private lateinit var crcFilePickerLauncher: ActivityResultLauncher<Array<String>>
    private var selectedCrcUri: Uri? = null

    @SuppressLint("TimberArgCount")
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Timber.d("Camera", "사진 URI: $currentPhotoUri")
            dataBinding.icStep3.ivSelectedImage.loadImage(currentPhotoUri.toString(), roundedCorner = 4.dpToPx())
            dataBinding.icStep3.ivCamera.isVisible = false
            dataBinding.icStep3.tvAddImage.isVisible = false
            viewModel.setProfileImage(currentPhotoUri)
            viewModel.profileFile.value = uriToFile(requireContext(), currentPhotoUri)
        } else {
            Timber.d("Camera", "사진 촬영 실패 또는 취소")
        }
    }


    @SuppressLint("TimberArgCount")
    override fun init() {
        super.init()

        // 권한 요청 런처
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result.all { it.value }
            if (granted) {
                openGallery()
            } else {
                requireContext().toast("이미지 접근 권한이 필요합니다.")
            }
        }

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                requireContext().toast("카메라 권한이 필요합니다.")
            }
        }

        // 이미지 선택 런처
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    // 여기서 이미지 URI 사용
                    Timber.d("ImageSelect", "선택된 이미지 URI: $it")
                    dataBinding.icStep3.ivSelectedImage.loadImage(it.toString(), roundedCorner = 4.dpToPx())
                    dataBinding.icStep3.ivCamera.isVisible = false
                    dataBinding.icStep3.tvAddImage.isVisible = false
                    viewModel.setProfileImage(imageUri)
                    viewModel.profileFile.value = uriToFile(requireContext(), imageUri)
                }
            }
        }

        setupFilePicker()

        viewModel.getPlayers(args.isResume)
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.icStep3.tvRegisterCRC.setOnClickListener {
            openCrcFilePicker()
        }
        dataBinding.icStep3.tvReRegisterCRC.setOnClickListener {
            openCrcFilePicker()
        }
        dataBinding.icStep3.etIntroduce.addTextChangedListener {
            viewModel.setIntroduceText(it?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.bankList.collectLatest { list ->

                        val bankNames = list.map { it.name } // BaseDto.name

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            bankNames
                        )

                        dataBinding.icStep2.etBank.setAdapter(adapter)

                        dataBinding.icStep2.etBank.setOnClickListener {
                            dataBinding.icStep2.etBank.showDropDown()
                        }

                        dataBinding.icStep2.etBank.setOnItemClickListener { _, _, position, _ ->
                            val selected = list[position]
                            dataBinding.icStep2.etBank.setText(selected.name, false) // 드랍다운 선택 반영
                            viewModel.setSelectedBank(selected)
                        }

                        viewModel.selectedBank.value?.name
                            ?.takeIf { it.isNotBlank() }
                            ?.let { dataBinding.icStep2.etBank.setText(it, false) }
                    }
                }

                launch {
                    viewModel.playerApplyedInfo.collectLatest { info ->
                        info ?: return@collectLatest

                        val profileImage = info.profile_image.orEmpty()
                        if (profileImage.isNotBlank()) {
                            dataBinding.icStep3.ivSelectedImage.loadImage(profileImage, roundedCorner = 4.dpToPx())
                            dataBinding.icStep3.ivCamera.isVisible = false
                            dataBinding.icStep3.tvAddImage.isVisible = false
                        }

                        val introduction = info.introduction.orEmpty()
                        if (
                            introduction.isNotBlank() &&
                            dataBinding.icStep3.etIntroduce.text?.toString() != introduction
                        ) {
                            dataBinding.icStep3.etIntroduce.setText(introduction)
                        }

                        val criminalFileName = info.criminalrecord_file_name.orEmpty()
                        if (criminalFileName.isNotBlank()) {
                            dataBinding.icStep3.tvRegisterCRC.isVisible = false
                            dataBinding.icStep3.tvCRCfileName.isVisible = true
                            dataBinding.icStep3.tvReRegisterCRC.isVisible = true
                            dataBinding.icStep3.tvCRCfileName.text = criminalFileName
                        }
                    }
                }

                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {

                        launch {
                            viewModel.startArea.collectLatest { area ->
                                if (area != null) {
                                    dataBinding.icStep4.tvDepart.isVisible = false
                                    dataBinding.icStep4.clDepart.isVisible = true
                                    dataBinding.icStep4.tvDepartValue.text = area.name
                                    dataBinding.icStep4.tvDepartAreaValue.text = "${viewModel.startAreaRadius.value}km"
                                } else {
                                    dataBinding.icStep4.tvDepart.isVisible = true
                                    dataBinding.icStep4.clDepart.isVisible = false
                                    dataBinding.icStep4.tvDepartValue.text = ""
                                    dataBinding.icStep4.tvDepartAreaValue.text = ""
                                }
                            }
                        }

                        launch {
                            viewModel.destArea.collectLatest { area ->
                                if (area != null) {
                                    dataBinding.icStep4.tvArrive.isVisible = false
                                    dataBinding.icStep4.clArrive.isVisible = true
                                    dataBinding.icStep4.tvArriveValue.text = area.name
                                    dataBinding.icStep4.tvArriveAreaValue.text = "${viewModel.destAreaRadius.value}km"
                                } else {
                                    dataBinding.icStep4.tvArrive.isVisible = true
                                    dataBinding.icStep4.clArrive.isVisible = false
                                    dataBinding.icStep4.tvArriveValue.text = ""
                                    dataBinding.icStep4.tvArriveAreaValue.text = ""
                                }
                            }
                        }

                        launch {
                            viewModel.startArea2.collectLatest { area ->
                               if(area != null){
                                 dataBinding.icStep4.tvDepart2.isVisible = false
                                 dataBinding.icStep4.clDepart2.isVisible = true
                                 dataBinding.icStep4.tvDepartValue2.text = area.name
                                 dataBinding.icStep4.tvDepartAreaValue2.text = "${viewModel.startAreaRadius2.value}km"
                               } else {
                                   resetSecondaryDepartUi()
                               }
                            }
                        }

                        launch {
                            viewModel.destArea2.collectLatest { area ->
                                if(area != null){
                                    dataBinding.icStep4.tvArrive2.isVisible = false
                                    dataBinding.icStep4.clArrive2.isVisible = true
                                    dataBinding.icStep4.tvArriveValue2.text = area.name
                                    dataBinding.icStep4.tvArriveAreaValue2.text = "${viewModel.destAreaRadius2.value}km"
                                } else {
                                    resetSecondaryArriveUi()
                                }
                            }
                        }

                        launch {
                            viewModel.deleteArea.collectLatest {
                                if(it) {
                                    viewModel.onDeleteArea(false)
                                    MessageDialog.newInstance(
                                        msg = "정말 삭제하시겠어요?",
                                        rightBtn = "네",
                                        leftBtn = "아니오"
                                    ).onRightBtn {
                                        viewModel.deleteSecondaryArea { success ->
                                            if (success) {
                                                resetSecondaryAreaUi()
                                                requireContext().toast("삭제가 완료되었습니다.")
                                            } else {
                                                requireContext().toast("삭제에 실패했습니다.")
                                            }
                                        }
                                    }.show(
                                        childFragmentManager,
                                        ""
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }

        dataBinding.icStep2.etAccountNumber.addTextChangedListener {
            viewModel.setAccountNumber(it?.toString().orEmpty())
        }

        dataBinding.icStep2.etName.addTextChangedListener {
            viewModel.setDepositorName(it?.toString().orEmpty())
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            "fromPlayerJoinSearch",
            viewLifecycleOwner
        ) { _, bundle ->

            val result =
                bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                    ?: return@setFragmentResultListener

            val isStart = bundle.getBoolean("isStart")
            val areaRadius = bundle.getInt("areaRadius")
            val isSecondary = bundle.getBoolean("isSecondary")

            if (isStart) {
                if(isSecondary){
                    viewModel.setStartAreaRadius2(areaRadius)
                    viewModel.setStartArea2(result)
                    saveSecondaryAreaIfReady()
                }else{
                    viewModel.setStartAreaRadius(areaRadius)
                    viewModel.setStartArea(result)
                }
            } else {
                if(isSecondary){
                    viewModel.setDestAreaRadius2(areaRadius)
                    viewModel.setDestArea2(result)
                    saveSecondaryAreaIfReady()
                }else{
                    viewModel.setDestAreaRadius(areaRadius)
                    viewModel.setDestArea(result)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                PlayerJoinViewModel.Event.Back ->{
                    if(viewModel.step.value == 1) {
                        findNavController().popBackStack()
                    }else{
                        viewModel.onClickStepNext(viewModel.step.value?.minus(1)?:1)
                    }
                }
                PlayerJoinViewModel.Event.Cancel -> {
                    showCancelPlayerApplicationDialog()
                }
                PlayerJoinViewModel.Event.AttachImage ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomPictureTypeDialog().apply {
                            pictureTypeSelectCallback = { pictureType ->
                                when(pictureType){
                                    PictureType.TYPE_CAMERA ->{
                                        openCamera()
                                    }
                                    PictureType.TYPE_GALLERY ->{
                                        requestImagePick()
                                    }
                                }
                            }
                        }
                    )
                }
                PlayerJoinViewModel.Event.CheckBankAccount -> {
                    showBankAccountConfirmDialog()
                }
                PlayerJoinViewModel.Event.StartArea ->{
                    val action =
                    PlayerJoinFragmentDirections.actionPlayerJoinFragment2ToPlayerJoinSearchFragment(true, false)
                    findNavController().navigate(action)
                    /*dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog()
                    )*/
                }
                PlayerJoinViewModel.Event.DestinaitonArea ->{
                    val action =
                        PlayerJoinFragmentDirections.actionPlayerJoinFragment2ToPlayerJoinSearchFragment(false, false)
                    findNavController().navigate(action)
                    /*dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog()
                    )*/
                }

                PlayerJoinViewModel.Event.StartArea2 ->{
                    val action =
                        PlayerJoinFragmentDirections.actionPlayerJoinFragment2ToPlayerJoinSearchFragment(true, true)
                    findNavController().navigate(action)
                }
                PlayerJoinViewModel.Event.DestinaitonArea2 ->{
                    val action =
                        PlayerJoinFragmentDirections.actionPlayerJoinFragment2ToPlayerJoinSearchFragment(false, true)
                    findNavController().navigate(action)
                }
                is PlayerJoinViewModel.Event.ShowMessage ->{
                    requireContext().toast(it.message)
                }
                is PlayerJoinViewModel.Event.TermDetail -> {
                    findNavController().navigate(
                        R.id.termDetailFragment,
                        bundleOf(
                            "title" to it.title,
                            "content" to it.content
                        )
                    )
                }
                PlayerJoinViewModel.Event.ProfileNext -> {
                    val playerId = viewModel.playerApplyedInfo.value?.player_id
                    if (playerId == null) {
                        requireContext().toast("플레이어 신청 정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.")
                        viewModel.getPlayers()
                        return@observe
                    }

                    viewModel.getApplyValidationMessage()?.let { message ->
                        requireContext().toast(message)
                        return@observe
                    }

                    viewModel.submitPlayerProfile(playerId) { success, message ->
                        if (success) {
                            viewModel.onClickStepNext(4)
                        } else {
                            requireContext().toast(message ?: "플레이어 정보를 저장하지 못했습니다.")
                        }
                    }
                }
                PlayerJoinViewModel.Event.Complete ->{
                    val playerId = viewModel.playerApplyedInfo.value?.player_id
                    if (playerId == null) {
                        requireContext().toast("플레이어 신청 정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.")
                        viewModel.getPlayers()
                        return@observe
                    }

                    viewModel.getApplyValidationMessage()?.let { message ->
                        requireContext().toast(message)
                        return@observe
                    }

                    viewModel.requestPlayerApply(playerId){ success, message ->
                        if(success){
                            val action = PlayerJoinFragmentDirections.actionPlayerJoinFragment2ToPlayerJoinCompleteFragment()
                            findNavController().navigate(action)
                        } else {
                            requireContext().toast(message ?: "플레이어 신청 제출에 실패했습니다.")
                        }
                    }
                }
                else ->{}
            }
        }
    }

    private fun showCancelPlayerApplicationDialog() {
        MessageDialog.newInstance(
            msg = "플레이어 신청을 취소하시겠어요?",
            rightBtn = "예",
            leftBtn = "아니오"
        ).onRightBtn {
            viewModel.cancelPlayerApplication { success, message ->
                if (success) {
                    findNavController().popBackStack(R.id.myFragment, false)
                } else {
                    requireContext().toast(message ?: "플레이어 신청 취소에 실패했습니다.")
                }
            }
        }.show(childFragmentManager, "")
    }

    private fun showBankAccountConfirmDialog() {
        val playerId = viewModel.playerApplyedInfo.value?.player_id
        if (playerId == null) {
            requireContext().toast("플레이어 신청 정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.")
            viewModel.getPlayers()
            return
        }

        val bank = viewModel.selectedBank.value
        if (bank == null) {
            requireContext().toast("은행을 선택해 주세요.")
            return
        }

        hideStep2Keyboard()
        dataBinding.root.postDelayed({
            if (!isAdded || !viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                return@postDelayed
            }
            if (childFragmentManager.findFragmentByTag(BANK_ACCOUNT_CONFIRM_DIALOG_TAG) != null) {
                return@postDelayed
            }

            BottomAccountInfoDialog().apply {
                bankName = bank.name
                bankCode = bank.code
                accountNumber = this@PlayerJoinFragment.viewModel.accountNumber.value.orEmpty()
                onConfirmClick = {
                    this@PlayerJoinFragment.viewModel.postPlayerBank(
                        playerId = playerId,
                        requestBank = BankRequestDto(
                            term_cds = this@PlayerJoinFragment.viewModel.terms.value?.map { it.code }
                                ?: listOf("terms_1", "terms_2", "terms_3"),
                            account_number = this@PlayerJoinFragment.viewModel.accountNumber.value.orEmpty(),
                            account_depositor = this@PlayerJoinFragment.viewModel.depositorName.value.orEmpty(),
                            bank_cd = bank.code
                        )
                    ) { success, message ->
                        Timber.d("saved bank phase $success")
                        if (success) {
                            this@PlayerJoinFragment.viewModel.onClickStepNext(3)
                        } else {
                            requireContext().toast(message ?: "계좌 정보를 다시 확인해 주세요.")
                        }
                    }
                }
            }.show(childFragmentManager, BANK_ACCOUNT_CONFIRM_DIALOG_TAG)
        }, KEYBOARD_DISMISS_DELAY_MS)
    }

    private fun hideStep2Keyboard() {
        val focusedView = requireActivity().currentFocus ?: dataBinding.root.findFocus() ?: dataBinding.icStep2.etName
        focusedView.clearFocus()
        dataBinding.icStep2.etAccountNumber.clearFocus()
        dataBinding.icStep2.etName.clearFocus()
        requireContext().hideKeyboard(focusedView)
    }

    private fun openCrcFilePicker() {
        crcFilePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
    }

    private fun setupFilePicker(){
        crcFilePickerLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri ?: return@registerForActivityResult

                selectedCrcUri = uri

                // 권한 유지(앱 재시작해도 접근 가능하게)
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val fileName = getFileNameFromUri(requireContext(), uri)

                dataBinding.icStep3.tvRegisterCRC.isVisible = false
                dataBinding.icStep3.tvCRCfileName.isVisible = true
                dataBinding.icStep3.tvReRegisterCRC.isVisible = true

                dataBinding.icStep3.tvCRCfileName.text = fileName
                viewModel.criminalFile.value = uriToFile(requireContext(), uri)
                viewModel.documentFileName.value = getFileNameFromUri(requireContext(), uri)
                viewModel.setCrcFile(uri)
            }
    }

    private fun saveSecondaryAreaIfReady() {
        if (viewModel.startArea2.value == null || viewModel.destArea2.value == null) return

        viewModel.saveSecondaryAreaIfReady { success ->
            if (!success) {
                requireContext().toast("동행범위2 저장에 실패했습니다.")
            }
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val photoFile = createImageFile(requireContext())
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(currentPhotoUri)
        }else{
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, imageFileName)
    }

    private fun requestImagePick() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val denied = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (denied.isEmpty()) {
            openGallery()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "unknown_file"

        runCatching {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }.onFailure { e -> e.printStackTrace() }
        return name
    }

    private fun resetSecondaryAreaUi() {
        resetSecondaryDepartUi()
        resetSecondaryArriveUi()
    }

    private fun resetSecondaryDepartUi() {
        dataBinding.icStep4.tvDepart2.isVisible = true
        dataBinding.icStep4.clDepart2.isVisible = false
        dataBinding.icStep4.tvDepartValue2.text = ""
        dataBinding.icStep4.tvDepartAreaValue2.text = ""
    }

    private fun resetSecondaryArriveUi() {
        dataBinding.icStep4.tvArrive2.isVisible = true
        dataBinding.icStep4.clArrive2.isVisible = false
        dataBinding.icStep4.tvArriveValue2.text = ""
        dataBinding.icStep4.tvArriveAreaValue2.text = ""
    }

    private fun convertUriToBase64(uri: Uri?): String {
        if (uri == null) return ""

        return runCatching {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }.getOrDefault("")
    }

    companion object {
        private const val BANK_ACCOUNT_CONFIRM_DIALOG_TAG = "bankAccountConfirmDialog"
        private const val KEYBOARD_DISMISS_DELAY_MS = 220L
    }
}
