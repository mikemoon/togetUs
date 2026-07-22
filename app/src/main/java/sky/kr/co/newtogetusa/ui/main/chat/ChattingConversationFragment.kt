package sky.kr.co.newtogetusa.ui.main.chat

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
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
import sky.kr.co.newtogetusa.NavGraphDirections
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.databinding.FragmentChattingConversationBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.MainViewModel
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomChatMoreDialog
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.ImageUtil
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.hideKeyboard
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.showKeyboard
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
    private var isInputToolsOpen = false
    private var keepLatestMessageAnchored = false
    private var pendingMediaType: PendingMediaType = PendingMediaType.IMAGE

    // 카메라를 실행한 후 찍은 사진을 저장
    var pictureUri: Uri? = null
    private val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capturedUri = pictureUri
        pictureUri = null
        if (success && capturedUri != null) {
            sendPickedImage(capturedUri, capturedUri)
        } else {
            capturedUri?.let(::deleteCapturedImage)
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
        setupInputControls()
    }


    override fun initObserver() {
        super.initObserver()

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val wasAtBottom = isMessageListAtBottom()
            val newLastMessage = messages.lastOrNull()
            val isNewOwnLatestMessage =
                hasLoadedInitialMessages &&
                    newLastMessage != null &&
                    newLastMessage.id != lastDisplayedMessageId &&
                    newLastMessage.isMyMessage
            // iOS와 동일하게 방에 처음 진입해 메시지를 불러온 직후에도 최신 메시지를 입력창 위에 표시한다.
            val shouldScrollToBottom = !hasLoadedInitialMessages || wasAtBottom || isNewOwnLatestMessage
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
                            dataBinding.ivProduct.loadImage(
                                imageUrl,
                                placeholder = R.drawable.no_img,
                                error = R.drawable.no_img,
                                roundedCorner = 4.dpToPx()
                            )
                        }
                    }
                }
                launch {
                    viewModel.isBlockedFlow.collect { isBlocked ->
                        setChatInputEnabled(!isBlocked)
                    }
                }
            }
        }

        viewModel.chatInputEnabled.observe(viewLifecycleOwner) { enabled ->
            setChatInputEnabled(enabled)
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
                            isBlocked = this@ChattingConversationFragment.viewModel.isBlockedFlow.value
                            isNotificationOn = this@ChattingConversationFragment.viewModel.isNotificationOnFlow.value
                            isReported = this@ChattingConversationFragment.viewModel.isReportedFlow.value
                            reportAction = {
                                this@ChattingConversationFragment.findNavController().navigate(
                                    ChattingConversationFragmentDirections.actionChattingConversationFragmentToChattingReportFragment(args.roomId)
                                )
                            }
                            alarmOnAction = {
                                this@ChattingConversationFragment.viewModel.setChatRoomNotificationOn()
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
                ChattingConversationViewModel.Event.DeliveryDetail -> {
                    openDeliveryDetail()
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
                    toggleInputTools()
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
                ChattingConversationViewModel.Event.ChatRoomNotificationOn -> {
                    requireContext().toast("채팅방의 알림이 켜졌습니다.")
                    viewModel.isNotificationOnFlow.value = true
                }
                ChattingConversationViewModel.Event.ChatRoomNotificationOff -> {
                    requireContext().toast("채팅방의 알림이 꺼졌습니다.")
                    viewModel.isNotificationOnFlow.value = false
                }
                ChattingConversationViewModel.Event.ChatRoomBlocked -> {
                    requireContext().toast("채팅방이 차단되었습니다.")
                    ChatRoomListUpdateBus.notifyRoomUpdated(args.roomId)
                    findNavController().popBackStack()
                }
                ChattingConversationViewModel.Event.ChatRoomExited -> {
                    requireContext().toast("채팅방을 나갔습니다.")
                    ChatRoomListUpdateBus.notifyRoomUpdated(args.roomId)
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun openDeliveryDetail() {
        val deliveryId = viewModel.deliveryIdFlow.value
        if (deliveryId <= 0L) {
            requireContext().toast("동행요청 정보를 확인할 수 없습니다.")
            return
        }

        if (args.isPlayerRoom || mainViewModel.isPlayerModeFlow.value) {
            findNavController().navigate(
                NavGraphDirections.actionGlobalPlayerHistoryDetailFragment(deliveryId)
            )
        } else {
            findNavController().navigate(
                NavGraphDirections.actionGlobalHistoryDetailFragment(createDeliverySummary(deliveryId))
            )
        }
    }

    private fun createDeliverySummary(deliveryId: Long): DeliverySummaryDto {
        return DeliverySummaryDto(
            delivery_id = deliveryId,
            requester_id = 0L,
            player_id = null,
            title = viewModel.deliveryTitleFlow.value,
            status_cd = viewModel.deliveryStatusFlow.value,
            prd_picture = viewModel.deliveryImageFlow.value,
            depart_address = "",
            dest_address = "",
            pickup_immediately = false,
            pickup_date = "",
            fee_final = 0,
            regist_date = null,
            apply_date = null,
            status_text = "",
            price_text = "",
            pickup_ui_date = "",
            regist_date_text = ""
        ).apply {
            setStatusText()
        }
    }

    private fun setChatInputEnabled(enabled: Boolean) {
        dataBinding.textInputLayout.isEnabled = enabled
        dataBinding.editTextMessage.isEnabled = enabled
        dataBinding.textInputLayout.hint = if (enabled) "메시지 입력" else "상대방과 대화가 불가능합니다."
        dataBinding.editTextMessage.hint = if (enabled) "메시지 입력" else "상대방과 대화가 불가능합니다."
        dataBinding.buttonSend.isEnabled = enabled
        dataBinding.buttonSend.isVisible = enabled
        dataBinding.ivPlus.isEnabled = enabled
        dataBinding.ivPlus.isVisible = enabled
        if (!enabled) {
            dataBinding.editTextMessage.clearFocus()
            dataBinding.editTextMessage.text = null
            isInputToolsOpen = false
            dataBinding.clInputTools.isVisible = false
            requireContext().hideKeyboard(dataBinding.editTextMessage)
        }
    }

    private fun setupInputControls() {
        dataBinding.editTextMessage.setOnClickListener {
            if (isInputToolsOpen) {
                setInputToolsOpen(open = false, showKeyboard = false)
            }
        }
        dataBinding.editTextMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && isInputToolsOpen) {
                setInputToolsOpen(open = false, showKeyboard = false)
            }
        }
    }

    private fun toggleInputTools() {
        if (isInputToolsOpen) {
            setInputToolsOpen(open = false, showKeyboard = true)
        } else {
            setInputToolsOpen(open = true, showKeyboard = false)
        }
    }

    private fun setInputToolsOpen(open: Boolean, showKeyboard: Boolean) {
        if (isInputToolsOpen == open && dataBinding.clInputTools.isVisible == open) {
            if (showKeyboard) showSoftKeyboard()
            return
        }

        isInputToolsOpen = open
        dataBinding.clInputTools.isVisible = open
        animatePlusButton(open)

        if (open) {
            dataBinding.editTextMessage.clearFocus()
            requireContext().hideKeyboard(dataBinding.editTextMessage)
        } else if (showKeyboard) {
            showSoftKeyboard()
        }
    }

    private fun showSoftKeyboard() {
        dataBinding.editTextMessage.requestFocus()
        dataBinding.editTextMessage.post {
            requireContext().showKeyboard(dataBinding.editTextMessage)
        }
    }

    private fun animatePlusButton(toClose: Boolean) {
        dataBinding.ivPlus.animate()
            .rotation(if (toClose) 45f else 0f)
            .setDuration(PLUS_BUTTON_ANIMATION_MS)
            .start()
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(viewModel, ::onChatMediaRendered)
        dataBinding.recyclerViewMessages.apply {
            adapter = this@ChattingConversationFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING && dy < 0) {
                        keepLatestMessageAnchored = false
                    } else if (isMessageListAtBottom()) {
                        keepLatestMessageAnchored = true
                    }
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

        // iPhone의 scrollToRow(..., .bottom)처럼 마지막 행을 배치한 다음,
        // 콘텐츠 높이와 뷰포트 높이로 계산한 실제 최하단 오프셋으로 이동한다.
        val recyclerView = dataBinding.recyclerViewMessages
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        keepLatestMessageAnchored = true
        layoutManager.scrollToPositionWithOffset(lastPosition, 0)
        recyclerView.doOnNextLayout {
            scrollRecyclerViewToBottom()
            recyclerView.post(::scrollRecyclerViewToBottom)
        }
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

    private fun scrollRecyclerViewToBottom() {
        val recyclerView = dataBinding.recyclerViewMessages
        val maxOffset = (
            recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()
            ).coerceAtLeast(0)
        recyclerView.scrollBy(0, maxOffset - recyclerView.computeVerticalScrollOffset())
    }

    private fun onChatMediaRendered() {
        if (!keepLatestMessageAnchored || !isAdded) return
        dataBinding.recyclerViewMessages.post(::scrollToLatestMessage)
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

    private fun sendPickedImage(uri: Uri, capturedUri: Uri? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            val encodedImage = withContext(Dispatchers.IO) {
                ImageUtil.uriToJpegBase64(requireContext(), uri)
            }

            when {
                encodedImage == null -> {
                    capturedUri?.let(::deleteCapturedImage)
                    requireContext().toast("이미지를 불러오지 못했습니다.")
                }
                encodedImage.byteSize > MAX_CHAT_IMAGE_BYTES -> {
                    capturedUri?.let(::deleteCapturedImage)
                    requireContext().toast("이미지 용량이 너무 큽니다.")
                }
                else -> {
                    setInputToolsOpen(open = false, showKeyboard = false)
                    viewModel.sendImage(encodedImage.base64, encodedImage.mime) {
                        capturedUri?.let(::deleteCapturedImage)
                    }
                }
            }
        }
    }

    private fun deleteCapturedImage(uri: Uri) {
        runCatching {
            requireContext().contentResolver.delete(uri, null, null)
        }.onFailure { error ->
            Timber.w(error, "Failed to delete temporary chat camera image")
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

    override fun onResume() {
        super.onResume()
        ChatRoomForegroundTracker.enter(args.roomId)
    }

    override fun onPause() {
        ChatRoomForegroundTracker.exit(args.roomId)
        super.onPause()
    }

    private companion object {
        const val MAX_CHAT_IMAGE_BYTES = 100 * 1024 * 1024
        const val READ_REFRESH_DELAY_MS = 500L
        const val PLUS_BUTTON_ANIMATION_MS = 300L
    }

    private enum class PendingMediaType {
        IMAGE,
        VIDEO
    }
}
