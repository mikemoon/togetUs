package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingImageDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.loadImage

@AndroidEntryPoint
class ChattingImageDetailFragment : BaseFragment<FragmentChattingImageDetailBinding, ChattingImageDetailViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_chatting_image_detail
    override val viewModel: ChattingImageDetailViewModel by viewModels()
    val args: ChattingImageDetailFragmentArgs by navArgs()

    override fun init() {
        super.init()

        dataBinding.ivMessageImage.apply {
            loadImage(args.imageUrl)
        }

    }
}