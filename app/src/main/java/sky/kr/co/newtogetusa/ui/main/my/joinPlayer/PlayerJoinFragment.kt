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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.request.player.BankRequestDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.databinding.FragmentJoinPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomAreaSelectDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomPictureTypeDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.PictureType
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.FileUtil.copyUriToTempFile
import sky.kr.co.newtogetusa.utils.FileUtil.createFilePart
import sky.kr.co.newtogetusa.utils.FileUtil.createImagePart
import sky.kr.co.newtogetusa.utils.FileUtil.uriToFile
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
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

        viewModel.getPlayers()
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
                                }
                            }
                        }

                        launch {
                            viewModel.deleteArea.collectLatest {
                                if(it) {
                                    MessageDialog.newInstance(
                                        msg = "정말 삭제하시겠어요?",
                                        rightBtn = "네",
                                        leftBtn = "아니오"
                                    ).onRightBtn {
                                        viewModel.onAddArea(false)
                                        requireContext().toast("삭제가 완료되었습니다.")
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

        parentFragmentManager.setFragmentResultListener(
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
                }else{
                    viewModel.setStartAreaRadius(areaRadius)
                    viewModel.setStartArea(result)
                }
            } else {
                if(isSecondary){
                    viewModel.setDestAreaRadius2(areaRadius)
                    viewModel.setDestArea2(result)
                }else{
                    viewModel.setDestAreaRadius(areaRadius)
                    viewModel.setDestArea(result)
                }
            }
        }

        viewModel.step.observe(viewLifecycleOwner){ step ->
            when(step){
                3 ->{
                    val playerId = viewModel.playerApplyedInfo.value?.player_id ?: return@observe
                    viewModel.postPlayerBank(
                        playerId = playerId,
                        requestBank = BankRequestDto(
                            term_cds = viewModel.terms.value?.map { it.code }?:return@observe,
                            account_number = viewModel.accountNumber.value.orEmpty(),
                            account_depositor = viewModel.depositorName.value.orEmpty(),
                            bank_cd = viewModel.selectedBank.value?.code.orEmpty()
                        )
                    ){ result ->
                        Timber.d("saved bank phase $result")
                    }
                }
                4 ->{
                    val playerId = viewModel.playerApplyedInfo.value?.player_id ?: return@observe
                    viewModel.postPlayerProfileImage(
                        playerId = playerId,
                        file = createImagePart(viewModel.profileFile.value?:return@observe)
                    ){ result ->
                        Timber.d("saved profile image phase $result")
                    }
                    viewModel.postPlayerIntroduce(
                        playerId = playerId,
                        introduceText = viewModel.introduceText.value.orEmpty()
                    )
                    Timber.d("criminal size = ${viewModel.criminalFile.value?.length()?:0 / 1024} KB")
                    viewModel.postPlayerCriminalRecord(
                        playerId = playerId,
                        file = createFilePart(
                            partName = "file",//viewModel.documentFileName.value,
                            file = viewModel.criminalFile.value?:return@observe
                        )
                    )
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
                PlayerJoinViewModel.Event.Complete ->{
                    viewModel.requestPlayerApply(viewModel.playerApplyedInfo.value?.player_id ?: return@observe){ result ->
                        if(result){
                            val action = PlayerJoinFragmentDirections.actionPlayerJoinFragment2ToPlayerJoinCompleteFragment()
                            findNavController().navigate(action)
                        }
                    }
                }
                else ->{}
            }
        }
    }

    private fun openCrcFilePicker() {
        crcFilePickerLauncher.launch(arrayOf("application/pdf"))
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

    private fun convertUriToBase64(uri: Uri?): String {
        if (uri == null) return ""

        return runCatching {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }.getOrDefault("")
    }
}