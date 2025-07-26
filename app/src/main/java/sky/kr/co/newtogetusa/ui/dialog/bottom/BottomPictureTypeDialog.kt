package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomPictureTypeBinding

@AndroidEntryPoint
class BottomPictureTypeDialog : BottomBaseDialog<DialogBottomPictureTypeBinding, BottomPictureTypeViewModel>() {
    var pictureTypeSelectCallback: ((pictureType: PictureType) -> Unit)? = null

    override val layoutId: Int
        get() = R.layout.dialog_bottom_picture_type
    override val viewModel: BottomPictureTypeViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                BottomPictureTypeViewModel.Event.Camera -> {
                    pictureTypeSelectCallback?.invoke(PictureType.TYPE_CAMERA)
                    dismissAllowingStateLoss()
                }
                BottomPictureTypeViewModel.Event.Gallery ->{
                    pictureTypeSelectCallback?.invoke(PictureType.TYPE_GALLERY)
                    dismissAllowingStateLoss()
                }
            }
        }
    }
}

enum class PictureType{
    TYPE_CAMERA,
    TYPE_GALLERY
}