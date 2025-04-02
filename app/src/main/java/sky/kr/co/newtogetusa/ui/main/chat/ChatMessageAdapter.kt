package sky.kr.co.newtogetusa.ui.main.chat

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.databinding.ItemChatMessageBinding
import sky.kr.co.newtogetusa.utils.loadImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatMessageAdapter(private val viewModel: ChattingConversationViewModel) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val previousMessage = if (position > 0) messages[position - 1] else null
        val nextMessage = if (position > 0 && messages.size > position+1) messages[position + 1] else null
        holder.bind(messages[position], previousMessage, nextMessage)
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage, previousMessage: ChatMessage?, nextMessage: ChatMessage?) {
            binding.apply {
                textViewSender.text = message.sender
                textViewMessage.text = message.content

                cardViewMessage.isVisible = message.messageType == 0
                ivMessageImage.isVisible = message.messageType == 1
                clVideo.isVisible = message.messageType == 2
                if(message.messageType == 1){
                    ivMessageImage.apply {
                        loadImage(message.messageImageUrl)
                        setOnClickListener {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageImageSelect(message.messageImageUrl!!))
                        }
                    }
                }

                if(message.messageType == 2){
                    ivVideoThumnail.apply {
                        loadImage(message.messageImageUrl)
                        setOnClickListener {
                            viewModel.onEventClick(ChattingConversationViewModel.Event.MessageVideoSelect(message.messageVieoUrl!!))
                        }
                    }
                }

                val isSameMinuteMessage = previousMessage != null && !previousMessage.isMyMessage && !message.isMyMessage && isSameMinute(previousMessage.timestamp, message.timestamp)
                val isNextMessageSameMinute = nextMessage != null && !nextMessage.isMyMessage && !message.isMyMessage && isSameMinute(message.timestamp, nextMessage.timestamp)
                /*if (previousMessage != null && isSameMinute(previousMessage.timestamp, message.timestamp)) {
                    tvTimeSender.isVisible = false
                    tvTimeMy.isVisible = false
                } else {
                    tvTimeSender.isVisible = true
                    tvTimeMy.isVisible = true
                }*/

                if(!message.isMyMessage && !isSameMinuteMessage){
                    ivSender.visibility = View.VISIBLE
                }else if(!message.isMyMessage){
                    ivSender.visibility = View.INVISIBLE
                }else{
                    ivSender.visibility = View.GONE
                }

                if(!message.isMyMessage && !isNextMessageSameMinute){
                    tvTimeSender.isVisible = true
                }else if(!message.isMyMessage){
                    tvTimeSender.isVisible = false
                }else{
                    tvTimeSender.isVisible = true
                }

                tvTimeMy.isVisible = message.isMyMessage
                // 내 메시지인 경우 오른쪽 정렬 및 색상 변경
                if (message.isMyMessage) {
                    tvTimeMy.text = formatTime(message.timestamp)
                    cardViewMessage.setBackgroundDrawable(itemView.context.getDrawable(R.drawable.chat_message_my_bg))

                    // 오른쪽 정렬
                    val params = cardViewMessage.layoutParams as ConstraintLayout.LayoutParams
                    params.horizontalBias = 1.0f
                    cardViewMessage.layoutParams = params
                } else {
                    tvTimeSender.text = formatTime(message.timestamp)
                    cardViewMessage.setBackgroundDrawable(itemView.context.getDrawable(R.drawable.chat_message_sender_bg))

                    // 왼쪽 정렬
                    val params = cardViewMessage.layoutParams as ConstraintLayout.LayoutParams
                    params.horizontalBias = 0.0f
                    cardViewMessage.layoutParams = params
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
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
    }
}