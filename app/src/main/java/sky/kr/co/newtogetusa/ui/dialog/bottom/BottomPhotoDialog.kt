package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomPhotoBinding
import java.io.File

@AndroidEntryPoint
class BottomPhotoDialog : BottomBaseDialog<DialogBottomPhotoBinding, BottomPhotoViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_photo
    override val viewModel: BottomPhotoViewModel by viewModels()

    var selectUrl :((Uri) -> Unit)? = null

    private var photoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectUrl?.invoke(it)
            dismissAllowingStateLoss()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri?.let { uri ->
                selectUrl?.invoke(uri)
            }
            dismissAllowingStateLoss()
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it) {
                BottomPhotoViewModel.Event.TakeGallery -> {
                    pickImageLauncher.launch("image/*")
                }

                BottomPhotoViewModel.Event.TakePhoto -> {
                    val imageFile = File(requireContext().cacheDir, "images").apply {
                        if (!exists()) mkdirs()
                    }
                    val photoFile = File(imageFile, "photo_${System.currentTimeMillis()}.jpg")
                    photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        photoFile
                    )
                    takePictureLauncher.launch(photoUri)
                }
            }
        }
    }
}