package sky.kr.co.newtogetusa.ui.main.chat

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.databinding.FragmentChattingConversationBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.MainViewModel
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomChatMoreDialog
import sky.kr.co.newtogetusa.utils.ImageUtil
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
    private val mainViewModel: MainViewModel by activityViewModels()
    private val args: ChattingConversationFragmentArgs by navArgs()

    private lateinit var adapter: ChatMessageAdapter
    private var hasLoadedInitialMessages = false
    private var lastDisplayedMessageId: Long? = null
    private var lastUnreadRefreshMessageId: Long = 0
    private var pendingMediaType: PendingMediaType = PendingMediaType.IMAGE

    // 카메라를 실행한 후 찍은 사진을 저장
    var pictureUri: Uri? = null
    private val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            pictureUri?.let(::sendPickedImage)
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                requireContext().toast("권한을 허용해주세요.")
            }
        }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Timber.tag("PhotoPicker").d("Selected URI: $uri")
            when (pendingMediaType) {
                PendingMediaType.IMAGE -> sendPickedImage(uri)
                PendingMediaType.VIDEO -> requireContext().toast("동영상 전송은 준비 중입니다.")
            }
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
            refreshUnreadBadgeAfterRead(messages)
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
                    openCamera()
                }

                ChattingConversationViewModel.Event.InputAlbum -> {
                    pendingMediaType = PendingMediaType.IMAGE
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }

                ChattingConversationViewModel.Event.InputMovie -> {
                    pendingMediaType = PendingMediaType.VIDEO
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
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

    private fun sendPickedImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val encodedImage = withContext(Dispatchers.IO) {
                ImageUtil.uriToJpegBase64(requireContext(), uri)
            }

            when {
                encodedImage == null -> requireContext().toast("이미지를 불러오지 못했습니다.")
                encodedImage.byteSize > MAX_CHAT_IMAGE_BYTES -> requireContext().toast("이미지 용량이 너무 큽니다.")
                else -> {
                    dataBinding.clInputTools.isVisible = false
                    viewModel.sendImage(encodedImage.base64, encodedImage.mime)
                }
            }
        }
    }

    private fun refreshUnreadBadgeAfterRead(messages: List<ChatMessage>) {
        val lastMessageId = messages.lastOrNull { it.id > 0 }?.id ?: return
        if (lastMessageId <= 0) return
        if (lastMessageId == lastUnreadRefreshMessageId) return
        lastUnreadRefreshMessageId = lastMessageId

        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(READ_REFRESH_DELAY_MS)
            mainViewModel.refreshChatUnread()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        pictureUri = createImageFile()
        val uri = pictureUri
        if (uri == null) {
            requireContext().toast("이미지 파일을 생성하지 못했습니다.")
            return
        }
        getTakePicture.launch(uri)
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

    private companion object {
        const val MAX_CHAT_IMAGE_BYTES = 100 * 1024 * 1024
        const val READ_REFRESH_DELAY_MS = 500L
    }

    private enum class PendingMediaType {
        IMAGE,
        VIDEO
    }
}
