package sky.kr.co.newtogetusa.ui.main.global

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentTakePhotoBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class TakePhotoFragment : BaseFragment<FragmentTakePhotoBinding, TakePhotoVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_take_photo
    override val viewModel: TakePhotoVM by viewModels()


}