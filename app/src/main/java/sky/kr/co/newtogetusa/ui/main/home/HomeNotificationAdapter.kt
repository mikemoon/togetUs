package sky.kr.co.newtogetusa.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sky.kr.co.newtogetusa.data.remote.dto.my.NotificationDto
import sky.kr.co.newtogetusa.databinding.ItemHomeNotificationBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeNotificationAdapter(
    private val onItemClick: (NotificationDto) -> Unit
) : RecyclerView.Adapter<HomeNotificationAdapter.ViewHolder>() {

    private val items = mutableListOf<NotificationDto>()

    fun setItems(newItems: List<NotificationDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    class ViewHolder(
        private val binding: ItemHomeNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationDto, onItemClick: (NotificationDto) -> Unit) {
            binding.notification = item
            binding.tvDate.text = item.regDate.toDisplayDate()
            binding.root.alpha = if (item.isUnread) 1f else 0.32f
            binding.root.setOnClickListener {
                onItemClick(item)
            }
            binding.executePendingBindings()
        }

        private fun String.toDisplayDate(): String {
            val source = runCatching {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(this)
            }.getOrNull() ?: return this

            val now = Calendar.getInstance(Locale.KOREA)
            val date = Calendar.getInstance(Locale.KOREA).apply { time = source }

            return when {
                now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) -> {
                    SimpleDateFormat("a h:mm", Locale.KOREA).format(source)
                }
                now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
                    SimpleDateFormat("M월 d일", Locale.KOREA).format(source)
                }
                else -> {
                    SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(source)
                }
            }
        }
    }
}
