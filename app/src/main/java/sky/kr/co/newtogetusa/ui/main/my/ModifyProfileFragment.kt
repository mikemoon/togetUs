package sky.kr.co.newtogetusa.ui.main.my

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
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
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerProfileImageRequest
import sky.kr.co.newtogetusa.data.remote.request.user.ProfileImageRequest
import sky.kr.co.newtogetusa.databinding.FragmentModifyProfileBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomPictureTypeDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.PictureType
import sky.kr.co.newtogetusa.utils.CacheCleanup
import sky.kr.co.newtogetusa.utils.bitmapToBase64
import sky.kr.co.newtogetusa.utils.loadProfile
import sky.kr.co.newtogetusa.utils.resizeImageUri
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.io.File
import java.util.UUID

@AndroidEntryPoint
class ModifyProfileFragment : BaseFragment<FragmentModifyProfileBinding, ModifyProfileViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_modify_profile
    override val viewModel: ModifyProfileViewModel by viewModels()
    private val args: ModifyProfileFragmentArgs by navArgs()

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private val isPlayerMode: Boolean
        get() = args.isPlayer

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = currentPhotoUri
        val path = currentPhotoPath
        if (success && uri != null) {
            viewModel.isProfileImageChanged.value = true
            Timber.d("CameraCapture captured image URI: $uri, ${viewModel.isProfileImageChanged.value}")
            val resizedBitmap = resizeImageUri(requireContext(), uri)
            resizedBitmap?.let { bitmap ->
                dataBinding.ivProfile.loadProfile(bitmap)
            }
        }
        if (!path.isNullOrBlank()) {
            CacheCleanup.deleteCacheFile(requireContext(), File(path))
        }
        currentPhotoUri = null
        currentPhotoPath = null
    }

    var profile: ProfileDto? = null
    override fun init() {
        super.init()
        viewModel.isProfileImageChanged.value = false
        profile = args.profileDto
        dataBinding.profile = args.profileDto

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
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
                val data = result.data
                val imageUri = data?.data ?: data?.clipData?.getItemAt(0)?.uri
                imageUri?.let {
                    takeReadPermission(data, it)
                    // 여기서 이미지 URI 사용
                    viewModel.isProfileImageChanged.value = true
                    Timber.d("ImageSelect 선택된 이미지 URI: $it, ${viewModel.isProfileImageChanged.value}")
                    val resizedBitmap = resizeImageUri(requireContext(), it)
                    resizedBitmap?.let {  bitmap ->
                        dataBinding.ivProfile.loadProfile(bitmap)
                    }
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ModifyProfileViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ModifyProfileViewModel.Event.Save -> {
                    profile?.user?.let { user ->
                        val userId = user.user_id
                        val nickname = dataBinding.etName.text.toString()
                        val isNicknameChanged = nickname != user.nickname
                        val isProfileImageChanged = viewModel.isProfileImageChanged.value

                        when {
                            isNicknameChanged -> {
                                viewModel.setNickName(
                                    userId,
                                    hashMapOf("nickname" to nickname)
                                ) { result ->
                                    if (result) {
                                        if (isProfileImageChanged) {
                                            saveProfileImage(user)
                                        } else {
                                            saveSuccess()
                                        }
                                    }
                                }
                            }
                            isProfileImageChanged -> {
                                saveProfileImage(user)
                            }
                            else -> {
                                saveSuccess()
                            }
                        }
                    }
                }
                is ModifyProfileViewModel.Event.ProfileImage -> {
                    showPictureTypeDialog()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.errorMsg.collectLatest {
                    requireContext().toast(it)
                }
            }
        }
    }

    private fun saveProfileImage(user: ProfileDto.User) {
        val profileImageBase64 = bitmapToBase64(dataBinding.ivProfile.drawable.toBitmap())
        if (isPlayerMode) {
            viewModel.setPlayerProfileImage(
                user.player_id,
                PlayerProfileImageRequest(
                    mime = "image/jpeg",
                    base64 = profileImageBase64
                )
            ) { imageSetResult ->
                if (imageSetResult) {
                    viewModel.isProfileImageChanged.value = false
                    saveSuccess()
                }
            }
        } else {
            viewModel.setProfileImage(
                user.user_id,
                ProfileImageRequest(
                    mime = "image/jpeg",
                    base64 = profileImageBase64
                )
            ) { imageSetResult ->
                if (imageSetResult) {
                    viewModel.isProfileImageChanged.value = false
                    saveSuccess()
                }
            }
        }
    }

    private fun saveSuccess(){
        requireContext().toast("수정이 완료되었습니다.")
        findNavController().popBackStack()
    }

    // iOS 대응: 사진 촬영/사진불러오기 하단 팝업 표시
    private fun showPictureTypeDialog() {
        BottomPictureTypeDialog().apply {
            pictureTypeSelectCallback = { pictureType ->
                when (pictureType) {
                    PictureType.TYPE_CAMERA -> openCamera()
                    PictureType.TYPE_GALLERY -> requestImagePick()
                }
            }
        }.show(parentFragmentManager, "BottomPictureTypeDialog")
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val imageDir = File(requireContext().cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val photoFile = File(imageDir, "profile_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
            currentPhotoPath = photoFile.absolutePath
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(currentPhotoUri)
        } catch (e: SecurityException) {
            Timber.e(e, "Camera permission was revoked before launching camera")
            requireContext().toast("카메라 권한이 필요합니다.")
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Failed to create camera image uri")
            requireContext().toast("카메라를 실행할 수 없습니다.")
        }
    }

    private fun requestImagePick() {
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        try {
            imagePickerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Document picker is not available")
            val fallback = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            imagePickerLauncher.launch(Intent.createChooser(fallback, "사진 선택"))
        }
    }

    private fun takeReadPermission(intent: Intent?, uri: Uri) {
        val flags = intent?.flags ?: return
        if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return

        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure {
            Timber.d(it, "Persistable read permission is not supported for uri: $uri")
        }
    }
}
