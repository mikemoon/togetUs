package sky.kr.co.newtogetusa.ui.main.chat

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.databinding.FragmentChattingConversationBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomChatMoreDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.loadImage
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.net.toUri
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ChattingConversationFragment :
    BaseFragment<FragmentChattingConversationBinding, ChattingConversationViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_conversation
    override val viewModel: ChattingConversationViewModel by viewModels()

    private lateinit var adapter: ChatMessageAdapter

    // 카메라를 실행한 후 찍은 사진을 저장
    var pictureUri: Uri? = null
    private val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            pictureUri.let { }
        }
    }

    // 요청하고자 하는 권한들
    private val permissionList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    // 권한을 허용하도록 요청
    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.entries.all { it.value }
            if (!granted) {
                Toast.makeText(requireContext(), "권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                pictureUri = createImageFile()
                getTakePicture.launch(pictureUri)
            }
        }

    private val galleryRequestMutiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.entries.all { it.value }
            if (!granted) {
                Toast.makeText(requireContext(), "권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

    private val videoRequestMutiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.entries.all { it.value }
            if (!granted) {
                Toast.makeText(requireContext(), "권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            }
        }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Timber.tag("PhotoPicker").d("Selected URI: $uri")
        } else {
            Timber.tag("PhotoPicker").d("No media selected")
        }
    }


    override fun init() {
        super.init()
        viewModel.connect()

        dataBinding.viewModel = viewModel
        dataBinding.ivProduct.apply {
            loadImage(
                "https://img.danawa.com/prod_img/500000/065/932/img/19932065_1.jpg?shrink=330:*&_v=20230425180255"
            )
        }

        setupRecyclerView()
    }


    override fun initObserver() {
        super.initObserver()

        adapter.setMessages(
            listOf(
                ChatMessage(id = 1, sender = "", "안녕하세요", isMyMessage = true, timestamp = 123029, messageType = 0),
                ChatMessage(id = 2, sender = "", "배달은요?", isMyMessage = false, timestamp = 123132, messageType = 0),
                ChatMessage(id = 3, sender = "", "배달은요??", isMyMessage = false, timestamp = 123132, messageType = 0),
                ChatMessage(id = 4, sender = "", "배달은요??", isMyMessage = false, timestamp = 123132, messageType = 2, messageImageUrl = "https://cdn.pixabay.com/photo/2017/03/19/21/21/luwak-2157626_1280.jpg",
                    messageVieoUrl = "https://www.sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4"),
                ChatMessage(id = 5, sender = "", "배달은요??", isMyMessage = false, timestamp = 123132, messageType = 1, messageImageUrl = "https://dimg.donga.com/wps/NEWS/IMAGE/2024/01/25/123232196.4.jpg"),
                        ChatMessage(id = 6, sender = "", "배달은요??", isMyMessage = true, timestamp = 123132, messageType = 2, messageImageUrl = "https://cdn.pixabay.com/photo/2017/03/19/21/21/luwak-2157626_1280.jpg",
                messageVieoUrl = "https://www.sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4"),
            ChatMessage(id= 6, sender = "", "배달은요??", isMyMessage = true, timestamp = 123132, messageType = 1, messageImageUrl = "https://dimg.donga.com/wps/NEWS/IMAGE/2024/01/25/123232196.4.jpg")
            )
        )

        viewModel.event.observe(viewLifecycleOwner) { event ->
            Timber.d("event $event")
            when (event) {
                ChattingConversationViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                ChattingConversationViewModel.Event.PhoneCall ->{
                    val phoneNumber = "tel:114"  // 전화번호 설정
                    val intent = Intent(Intent.ACTION_DIAL, phoneNumber.toUri())
                    startActivity(intent)
                }

                ChattingConversationViewModel.Event.More -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomChatMoreDialog().apply {
                            reportAction = {
                                this@ChattingConversationFragment.findNavController().navigate(ChattingConversationFragmentDirections.actionChattingConversationFragmentToChattingReportFragment())
                            }
                            exitAction = {
                                this@ChattingConversationFragment.findNavController().popBackStack()
                            }
                        }
                    )
                }
                is ChattingConversationViewModel.Event.MessageImageSelect ->{
                    findNavController().navigate(ChattingConversationFragmentDirections.actionChattingConversationFragmentToChattingImageDetailFragment(event.url))
                }
                is ChattingConversationViewModel.Event .MessageVideoSelect ->{
                    findNavController().navigate(ChattingConversationFragmentDirections.actionChattingConversationFragmentToChattingVideoDetailFragment(event.url))
                }
                is ChattingConversationViewModel.Event.MessageResend ->{}
                is ChattingConversationViewModel.Event.MessageDelete ->{}

                ChattingConversationViewModel.Event.InputMore -> {
                    dataBinding.clInputTools.isVisible = !dataBinding.clInputTools.isVisible
                }

                ChattingConversationViewModel.Event.InputSend -> {

                }

                ChattingConversationViewModel.Event.InputCamera -> {
                    requestMultiplePermission.launch(permissionList)
                }

                ChattingConversationViewModel.Event.InputAlbum -> {
                    galleryRequestMutiplePermission.launch(mutableListOf<String>().apply {
                        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.toTypedArray())
                }

                ChattingConversationViewModel.Event.InputMovie -> {
                    videoRequestMutiplePermission.launch(mutableListOf<String>().apply {
                        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.toTypedArray())
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(viewModel)
        dataBinding.recyclerViewMessages.apply {
            adapter = this@ChattingConversationFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // 아래에서부터 쌓기
            }
        }
    }

    private fun createImageFile(): Uri? {
        val now = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_$now.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }
        return requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            content
        )
    }
}