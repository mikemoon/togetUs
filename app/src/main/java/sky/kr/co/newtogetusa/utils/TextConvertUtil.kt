package sky.kr.co.newtogetusa.utils

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TextConvertUtil {

    fun formatWon(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        return "${formatter.format(amount)}원"
    }

    fun formatPickupDateTime(date: String, time: String?): String {
        val normalizedDate = date.trim()
        if (normalizedDate.isBlank() || normalizedDate.equals("null", ignoreCase = true)) {
            return "-"
        }

        val localDate = runCatching {
            LocalDate.parse(normalizedDate, DateTimeFormatter.ofPattern("yyyyMMdd"))
        }.getOrNull() ?: return "-"

        val month = localDate.monthValue
        val day = localDate.dayOfMonth

        val dayOfWeek = when (localDate.dayOfWeek.value) {
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            else -> "일"
        }

        // time이 없으면 날짜만 표시
        if (time.isNullOrBlank() || time == "null") {
            return "${month}월 ${day}일(${dayOfWeek})"
        }

        val localTime = runCatching {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmm"))
        }.getOrNull()

        // 파싱 실패 시 날짜만 표시
        if (localTime == null) {
            return "${month}월 ${day}일(${dayOfWeek})"
        }

        val hour = localTime.hour
        val minute = localTime.minute

        val amPm = if (hour < 12) "오전" else "오후"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return "${month}월 ${day}일(${dayOfWeek}) $amPm $displayHour:${minute.toString().padStart(2, '0')}"
    }

    fun Int.toWon(): String {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        return "${formatter.format(this)}원"
    }

}
