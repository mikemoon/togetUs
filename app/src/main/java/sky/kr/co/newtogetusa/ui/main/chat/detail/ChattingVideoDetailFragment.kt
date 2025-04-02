package sky.kr.co.newtogetusa.ui.main.chat.detail

import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingVideoDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingVideoDetailFragment : BaseFragment<FragmentChattingVideoDetailBinding, ChattingVideoDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_video_detail
    override val viewModel: ChattingVideoDetailViewModel by viewModels()
    val args : ChattingVideoDetailFragmentArgs by navArgs()

    private lateinit var exoPlayer: ExoPlayer

    override fun init() {
        super.init()

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        dataBinding.videoPlayer.player = exoPlayer

        exoPlayer.setMediaItem(MediaItem.fromUri(args.videoUrl))
        exoPlayer.prepare()
        exoPlayer.play()
    }
}