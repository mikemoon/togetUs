package sky.kr.co.newtogetusa.ui.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.ChatMessage
import sky.kr.co.newtogetusa.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatMessageAdapter : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

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
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.apply {
                textViewSender.text = message.sender
                textViewMessage.text = message.content
                textViewTime.text = formatTime(message.timestamp)

                // 내 메시지인 경우 오른쪽 정렬 및 색상 변경
                if (message.isMyMessage) {
                    cardViewMessage.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, android.R.color.holo_blue_light)
                    )
                    textViewMessage.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )

                    // 오른쪽 정렬
                    val params = cardViewMessage.layoutParams as ConstraintLayout.LayoutParams
                    params.horizontalBias = 1.0f
                    cardViewMessage.layoutParams = params
                } else {
                    cardViewMessage.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
                    )
                    textViewMessage.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.black)
                    )

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
    }
}