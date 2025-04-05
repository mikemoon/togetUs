package sky.kr.co.newtogetusa.ui.main.chat.detail

import android.annotation.SuppressLint
import android.os.Handler
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingVideoDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ChattingVideoDetailFragment : BaseFragment<FragmentChattingVideoDetailBinding, ChattingVideoDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_video_detail
    override val viewModel: ChattingVideoDetailViewModel by viewModels()
    val args : ChattingVideoDetailFragmentArgs by navArgs()

    private val handler = Handler()
    private lateinit var exoPlayer: ExoPlayer
    private var totalPlayTime:String = "00:00"

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.lifecycleOwner = this
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        dataBinding.videoPlayer.player = exoPlayer

        exoPlayer.setMediaItem(MediaItem.fromUri(args.videoUrl))
        exoPlayer.prepare()
        exoPlayer.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    val totalDuration = exoPlayer.duration
                    dataBinding.seekbar.max = totalDuration.toInt()
                    totalPlayTime = formatTime(totalDuration)
                }
            }
        })
        exoPlayer.play()
        updateSeekBar()

        dataBinding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer.seekTo(progress.toLong())
                    dataBinding.tvTime.text = formatTime(progress.toLong())+"/"+totalPlayTime
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                ChattingVideoDetailViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                ChattingVideoDetailViewModel.Event.PausePlay ->{
                    exoPlayer.let {
                        if (it.isPlaying) {
                            it.pause()
                        } else {
                            it.play()
                            updateSeekBar()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer.release()
    }

    private fun updateSeekBar() {
        exoPlayer.let {
            dataBinding.seekbar.progress = it.currentPosition.toInt()
            dataBinding.tvTime.text = formatTime(it.currentPosition)+"/"+totalPlayTime
            handler.postDelayed({ updateSeekBar() }, 500)
        }
    }

    // 시간을 00:00 형식으로 변환
    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}