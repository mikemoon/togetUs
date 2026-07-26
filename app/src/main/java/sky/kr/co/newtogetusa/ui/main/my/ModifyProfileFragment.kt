package sky.kr.co.newtogetusa.ui.main.my

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import sky.kr.co.newtogetusa.utils.bitmapToBase64
import sky.kr.co.newtogetusa.utils.loadProfile
import sky.kr.co.newtogetusa.utils.resizeImageUri
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber

@AndroidEntryPoint
class ModifyProfileFragment : BaseFragment<FragmentModifyProfileBinding, ModifyProfileViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_modify_profile
    override val viewModel: ModifyProfileViewModel by viewModels()
    private val args: ModifyProfileFragmentArgs by navArgs()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val isPlayerMode: Boolean
        get() = args.isPlayer



    var profile: ProfileDto? = null
    override fun init() {
        super.init()
        viewModel.isProfileImageChanged.value = false
        profile = args.profileDto
        dataBinding.profile = args.profileDto

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

        // 이미지 선택 런처
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
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
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        imagePickerLauncher.launch(intent)
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
}
