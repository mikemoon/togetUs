package sky.kr.co.newtogetusa.ui.main.chat

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.databinding.ItemChatMessageBinding
import sky.kr.co.newtogetusa.databinding.ItemChatMessageOtherBinding
import sky.kr.co.newtogetusa.databinding.ItemChatMessageSystemBinding
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.loadImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatMessageAdapter(
    private val viewModel: ChattingConversationViewModel,
    private val onMediaRendered: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SYSTEM_MESSAGE -> {
                val binding = ItemChatMessageSystemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                MessageSystemViewHolder(binding)
            }
            VIEW_TYPE_MY_MESSAGE -> {
                val binding = ItemChatMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                MessageViewHolder(binding)
            }
            else -> {
                val binding = ItemChatMessageOtherBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                MessageOtherViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val previousMessage = if (position > 0) messages[position - 1] else null
        val nextMessage = if (position > 0 && messages.size > position+1) messages[position + 1] else null
        when(holder){
            is MessageSystemViewHolder -> holder.bind(messages[position])
            is MessageViewHolder -> holder.bind(messages[position], previousMessage, nextMessage)
            is MessageOtherViewHolder -> holder.bind(messages[position], previousMessage, nextMessage)
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].isSystem -> VIEW_TYPE_SYSTEM_MESSAGE
            messages[position].isMyMessage -> VIEW_TYPE_MY_MESSAGE
            else -> VIEW_TYPE_OTHER_MESSAGE
        }
    }

    inner class MessageSystemViewHolder(private val binding: ItemChatMessageSystemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvSystemMessage.text = message.content
        }
    }

    inner class MessageOtherViewHolder(private val binding: ItemChatMessageOtherBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(message: ChatMessage, previousMessage: ChatMessage?, nextMessage: ChatMessage?) {
            binding.apply {
                tvSender.text = message.sender
                textViewMessage.text = message.content
                val isSameMinuteMessage = previousMessage != null && !previousMessage.isSystem && !previousMessage.isMyMessage && isSameMinute(previousMessage.timestamp, message.timestamp)
                val isNextMessageSameMinute = nextMessage != null && !nextMessage.isSystem && !nextMessage.isMyMessage && isSameMinute(message.timestamp, nextMessage.timestamp)
                tvTimeSender.text = formatTime(message.timestamp)
                if(!isSameMinuteMessage){
                    ivSender.visibility = View.VISIBLE
                }else if(!message.isMyMessage){
                    ivSender.visibility = View.INVISIBLE
                }
                tvTimeSender.isVisible = !isNextMessageSameMinute

                cardViewMessage.isVisible = message.messageType == 0
                ivMessageImage.isVisible = message.messageType == 1
                clVideo.isVisible = message.messageType == 2
                if(message.messageType == 1){
                    ivMessageImage.apply {
                        loadImage(message.messageImageUrl, roundedCorner = 16.dpToPx(), onResourceReady = onMediaRendered)
                        setOnClickListener {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageImageSelect(message.messageImageUrl!!))
                        }
                    }
                }

                if(message.messageType == 2){
                    ivVideoThumnail.apply {
                        loadImage(message.messageImageUrl, roundedCorner = 16.dpToPx(), onResourceReady = onMediaRendered)
                        setOnClickListener {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageVideoSelect(message.messageVieoUrl!!))
                        }
                    }
                }
            }
        }
    }

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(message: ChatMessage, previousMessage: ChatMessage?, nextMessage: ChatMessage?) {
            binding.apply {
                textViewMessage.text = message.content

                cardViewMessage.isVisible = message.messageType == 0
                ivMessageImage.isVisible = message.messageType == 1
                clVideo.isVisible = message.messageType == 2
                if(message.messageType == 1){
                    ivMessageImage.apply {
                        loadImage(message.messageImageUrl, roundedCorner = 16.dpToPx(), onResourceReady = onMediaRendered)
                        setOnClickListener {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageImageSelect(message.messageImageUrl!!))
                        }
                    }
                }

                if(message.messageType == 2){
                    ivVideoThumnail.apply {
                        loadImage(message.messageImageUrl, roundedCorner = 16.dpToPx(), onResourceReady = onMediaRendered)
                        setOnClickListener {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageVideoSelect(message.messageVieoUrl!!))
                        }
                    }
                }

                val isSameMinuteMessage = previousMessage != null && !previousMessage.isSystem && !previousMessage.isMyMessage && !message.isMyMessage && isSameMinute(previousMessage.timestamp, message.timestamp)
                val isNextMessageSameMinute = nextMessage != null && !nextMessage.isSystem && !nextMessage.isMyMessage && !message.isMyMessage && isSameMinute(message.timestamp, nextMessage.timestamp)
                /*if (previousMessage != null && isSameMinute(previousMessage.timestamp, message.timestamp)) {
                    tvTimeSender.isVisible = false
                    tvTimeMy.isVisible = false
                } else {
                    tvTimeSender.isVisible = true
                    tvTimeMy.isVisible = true
                }*/

                tvTimeMy.text = formatTime(message.timestamp)
                tvMyRead.isVisible = message.isMyMessage && message.isUnread

                //보내기 실패
                ivSendFailed.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val touchedX = event.x
                        val width = view.width

                        if (touchedX < width / 2) {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageResend(message.id))
                        } else {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageDelete(message.id))
                        }
                    }
                    true
                }
            }
        }

    }

    private fun isSameMinute(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE)
    }

    private fun formatTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 0
        private const val VIEW_TYPE_SYSTEM_MESSAGE = 2
    }
}
