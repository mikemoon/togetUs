package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentJoinPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomAreaSelectDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomPictureTypeDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.PictureType
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

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Timber.d("Camera", "사진 URI: $currentPhotoUri")
            dataBinding.icStep3.ivSelectedImage.loadImage(currentPhotoUri.toString(), roundedCorner = 4.dpToPx())
            dataBinding.icStep3.ivCamera.isVisible = false
            dataBinding.icStep3.tvAddImage.isVisible = false
        } else {
            Timber.d("Camera", "사진 촬영 실패 또는 취소")
        }
    }


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
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

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
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog()
                    )
                }
                PlayerJoinViewModel.Event.DestinaitonArea ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelectDialog()
                    )
                }
                PlayerJoinViewModel.Event.Complete ->{
                    findNavController().navigate(R.id.action_playerJoinFragment_to_playerJoinCompleteFragment)
                }
                else ->{}
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
}