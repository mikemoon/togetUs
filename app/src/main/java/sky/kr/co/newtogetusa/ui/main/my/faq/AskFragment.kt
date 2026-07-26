package sky.kr.co.newtogetusa.ui.main.my.faq

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAskBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomPictureTypeDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.PictureType
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.CacheCleanup
import sky.kr.co.newtogetusa.utils.ImageUtil
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.io.File
import java.util.UUID

@AndroidEntryPoint
class AskFragment : BaseFragment<FragmentAskBinding, AskViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_ask
    override val viewModel: AskViewModel by viewModels()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    private var selectedImageUri: Uri? = null
    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                selectedImageUri = uri
                dataBinding.ivSelectedImage.loadImage(uri.toString(), roundedCorner = 4.dpToPx())
                dataBinding.ivCamera.isVisible = false
                dataBinding.tvAddImage.isVisible = false
                dataBinding.clImage.background = null
                updateSubmitState()
            }
        } else {
            currentPhotoPath?.let { path ->
                CacheCleanup.deleteCacheFile(requireContext(), File(path))
            }
        }
        currentPhotoUri = null
        currentPhotoPath = null
    }

    @SuppressLint("TimberArgCount")
    override fun init() {
        super.init()
        setupForm()

        // 권한 요청 런처 (갤러리용)
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

        // 카메라 권한 런처
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
                val imageUri = result.data?.data
                imageUri?.let {
                    selectedImageUri = it
                    Timber.d("ImageSelect", "선택된 이미지 URI: $it")
                    dataBinding.ivSelectedImage.loadImage(it.toString(), roundedCorner = 4.dpToPx())
                    dataBinding.ivCamera.isVisible = false
                    dataBinding.tvAddImage.isVisible = false
                    dataBinding.clImage.background = null
                    updateSubmitState()
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is AskViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is AskViewModel.Event.ShowPrivacy ->{
                    showPrivacyDialog()
                }
                AskViewModel.Event.AttachImage ->{
                    showPictureTypeDialog()
                }
            }
        }
    }

    private fun showPictureTypeDialog() {
        dialogFragmentShow(
            childFragmentManager,
            BottomPictureTypeDialog().apply {
                pictureTypeSelectCallback = { pictureType ->
                    when (pictureType) {
                        PictureType.TYPE_CAMERA -> openCamera()
                        PictureType.TYPE_GALLERY -> requestImagePick()
                    }
                }
            }
        )
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        val imageDir = File(requireContext().cacheDir, "images").apply {
            if (!exists()) mkdirs()
        }
        val photoFile = File(imageDir, "ask_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
        currentPhotoPath = photoFile.absolutePath
        currentPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(currentPhotoUri)
    }

    fun requestImagePick() {
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

    private fun setupForm() {
        val text = "투겟어스 개인정보 수집 및 활용에 동의합니다."
        val target = "개인정보 수집 및 활용"

        val spannable = SpannableString(text)
        val start = text.indexOf(target)
        val end = start + target.length

// 색상
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.primary_100)),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// 밑줄
        spannable.setSpan(
            UnderlineSpan(),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        dataBinding.tvGuide.text = spannable

        dataBinding.etContent.filters = arrayOf(InputFilter.LengthFilter(1000))
        dataBinding.tvContentCount.text = "0/1,000"

        dataBinding.etContent.doAfterTextChanged {
            val count = it?.length ?: 0
            dataBinding.tvContentCount.text = "$count/1,000"
            updateSubmitState()
        }

        dataBinding.cbPrivacy.setOnCheckedChangeListener { _, _ ->
            updateSubmitState()
        }

        dataBinding.tvSubmit.setOnClickListener {
            if (!isFormValid()) return@setOnClickListener

            val imageBase64 = selectedImageUri?.let { uri ->
                ImageUtil.uriListToPhotos(requireContext(), listOf(uri)).firstOrNull()?.base64
            }

            viewModel.postOneOnOne(
                content = dataBinding.etContent.text?.toString()?.trim().orEmpty(),
                phone = dataBinding.etPhone.text?.toString()?.trim().orEmpty(),
                imageBase64 = imageBase64
            ){ result ->
                if(result){
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val hasContent = !dataBinding.etContent.text.isNullOrBlank()
        val isChecked = dataBinding.cbPrivacy.isChecked
        val hasImage = selectedImageUri != null
        return hasContent && isChecked && hasImage
    }

    private fun updateSubmitState() {
        dataBinding.tvSubmit.isEnabled = isFormValid()
    }

    private fun showPrivacyDialog(){
        dialogFragmentShow(
            childFragmentManager,
            MessageDialog.newInstance(
                msgTitle = "개인정보 수집 및 활용",
                msg = "수집하는 개인 정보[(필수) 문의 내용, (선택) 휴대폰 번호, 첨부 파일]는 문의 내용 처리 및 고객 불만을 해결하기 위해 사용되며, 관련 법령에 따라 3년간 보관 후 삭제됩니다.\n" +
                        "\n" +
                        "문의 접수, 처리 및 회신을 위해 꼭 필요한 정보이기 때문에, 동의해 주셔야 서비스를 이용하실 수 있습니다.",
                rightBtn = "확인"
            )
        )
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }
}
