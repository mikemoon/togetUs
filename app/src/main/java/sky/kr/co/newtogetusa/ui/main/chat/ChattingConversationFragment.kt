package sky.kr.co.newtogetusa.ui.main.chat

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ChattingConversationFragment :
    BaseFragment<FragmentChattingConversationBinding, ChattingConversationViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_chatting_conversation
    override val viewModel: ChattingConversationViewModel by viewModels()
    private val args: ChattingConversationFragmentArgs by navArgs()

    private lateinit var adapter: ChatMessageAdapter
    private var hasLoadedInitialMessages = false
    private var lastDisplayedMessageId: Long? = null

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
        viewModel.loadRoomMessages(args.roomId)

        dataBinding.viewModel = viewModel
        dataBinding.ivProduct.setImageResource(R.drawable.no_img)

        setupRecyclerView()
    }


    override fun initObserver() {
        super.initObserver()

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val wasAtBottom = isMessageListAtBottom()
            val shouldScrollToBottom = !hasLoadedInitialMessages || wasAtBottom
            val newLastMessage = messages.lastOrNull()
            val shouldShowNewMessagePopup =
                hasLoadedInitialMessages &&
                    !wasAtBottom &&
                    newLastMessage != null &&
                    newLastMessage.id != lastDisplayedMessageId &&
                    !newLastMessage.isMyMessage
            adapter.setMessages(messages)
            hasLoadedInitialMessages = true
            lastDisplayedMessageId = newLastMessage?.id
            if (shouldScrollToBottom) {
                scrollToLatestMessage()
            } else if (shouldShowNewMessagePopup) {
                showNewMessagePopup(newLastMessage)
            } else {
                updateScrollToBottomButton()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deliveryImageFlow.collect { imageUrl ->
                        if (imageUrl.isNullOrBlank()) {
                            dataBinding.ivProduct.setImageResource(R.drawable.no_img)
                        } else {
                            dataBinding.ivProduct.loadImage(imageUrl, placeholder = R.drawable.no_img, error = R.drawable.no_img)
                        }
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            Timber.d("event $event")
            when (event) {
                ChattingConversationViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                ChattingConversationViewModel.Event.PhoneCall ->{
                    requireContext().toast("전화번호 정보가 없습니다.")
                }

                ChattingConversationViewModel.Event.More -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomChatMoreDialog().apply {
                            reportAction = {
                                this@ChattingConversationFragment.findNavController().navigate(
                                    ChattingConversationFragmentDirections.actionChattingConversationFragmentToChattingReportFragment(args.roomId)
                                )
                            }
                            alarmOffAction = {
                                this@ChattingConversationFragment.viewModel.setChatRoomNotificationOff()
                            }
                            blockAction = {
                                this@ChattingConversationFragment.viewModel.blockChatRoom()
                            }
                            exitAction = {
                                this@ChattingConversationFragment.viewModel.exitChatRoom()
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
                is ChattingConversationViewModel.Event.MessageResend -> viewModel.resendMessage(event.id)
                is ChattingConversationViewModel.Event.MessageDelete -> viewModel.removeMessage(event.id)

                ChattingConversationViewModel.Event.InputMore -> {
                    dataBinding.clInputTools.isVisible = !dataBinding.clInputTools.isVisible
                }

                ChattingConversationViewModel.Event.InputSend -> {
                    val message = dataBinding.editTextMessage.text?.toString().orEmpty()
                    viewModel.sendMessage(message)
                    dataBinding.editTextMessage.text = null
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
                is ChattingConversationViewModel.Event.ChatActionSuccess -> requireContext().toast(event.message)
                is ChattingConversationViewModel.Event.ChatActionFailed -> requireContext().toast(event.message)
                ChattingConversationViewModel.Event.ChatRoomExited -> {
                    requireContext().toast("채팅방을 나갔습니다.")
                    findNavController().popBackStack()
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
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    updateScrollToBottomButton()
                }
            })
        }
        dataBinding.fabScrollToBottom.setOnClickListener {
            scrollToLatestMessage()
        }
        dataBinding.tvNewMessagePopup.setOnClickListener {
            scrollToLatestMessage()
        }
    }

    private fun scrollToLatestMessage() {
        val lastPosition = adapter.itemCount - 1
        if (lastPosition < 0) {
            dataBinding.fabScrollToBottom.isVisible = false
            dataBinding.tvNewMessagePopup.isVisible = false
            return
        }

        dataBinding.recyclerViewMessages.scrollToPosition(lastPosition)
        dataBinding.fabScrollToBottom.isVisible = false
        dataBinding.tvNewMessagePopup.isVisible = false
    }

    private fun updateScrollToBottomButton() {
        val isAtBottom = isMessageListAtBottom()
        if (isAtBottom) {
            dataBinding.tvNewMessagePopup.isVisible = false
        }
        dataBinding.fabScrollToBottom.isVisible = !isAtBottom && !dataBinding.tvNewMessagePopup.isVisible
    }

    private fun showNewMessagePopup(message: ChatMessage) {
        val popupText = listOf(message.sender, message.content)
            .filter { it.isNotBlank() }
            .joinToString(": ")

        dataBinding.tvNewMessagePopup.text = popupText
        dataBinding.tvNewMessagePopup.isVisible = popupText.isNotBlank()
        dataBinding.fabScrollToBottom.isVisible = false
    }

    private fun isMessageListAtBottom(): Boolean {
        val layoutManager = dataBinding.recyclerViewMessages.layoutManager as? LinearLayoutManager ?: return true
        val lastItemPosition = adapter.itemCount - 1
        if (lastItemPosition < 0) return true

        return layoutManager.findLastCompletelyVisibleItemPosition() >= lastItemPosition
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
