package sky.kr.co.newtogetusa.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

val week = listOf(
    "일",
    "월",
    "화",
    "수",
    "목",
    "금",
    "토",
)

fun LocalDate.toKoreanDateYYYYMMDDEEEE(): String {
    val formatter = DateTimeFormatter.ofPattern(
        "yyyy년 MM월 dd일 EEEE",
        Locale.KOREAN
    )
    return this.format(formatter)
}

fun LocalDate.toYYYYMMDD(): String =
    this.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

fun formatPickupDateTime(raw: String): String {
    // "20250912 1430" or "20260120 226"
    val parts = raw.trim().split(Regex("\\s+"))
    if (parts.size != 2) return raw

    val datePart = parts[0]
    val timeToken = parts[1]
    if (datePart.length != 8 || timeToken.equals("null", ignoreCase = true)) return raw

    return runCatching {
        val timePart = timeToken.padStart(4, '0')
        val date = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyyMMdd"))
        val time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HHmm"))
        val amPm = if (time.hour < 12) "오전" else "오후"
        val hour12 = when {
            time.hour == 0 -> 12
            time.hour > 12 -> time.hour - 12
            else -> time.hour
        }
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)

        "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일($dayOfWeek) " +
                "$amPm ${"%01d".format(hour12)}:${"%02d".format(time.minute)}"
    }.getOrElse { raw }
    }

fun formatPickupDateTimeOrNow(raw: String): String {
    val source = raw.takeIf { it.isNotBlank() }
        ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HHmm"))
    return formatPickupDateTime(source)
}


fun formatRegisterDateTime(input: String): String {
    val normalized = input.trim()
        .takeIf { it.isNotBlank() }
        ?.replace('T', ' ')
        ?.removeSuffix("Z")
        ?.substringBefore("+")
        ?: return ""

    val flexibleDateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .optionalStart()
        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
        .optionalEnd()
        .toFormatter()

    val dateTime = runCatching {
        LocalDateTime.parse(normalized, flexibleDateTimeFormatter)
    }.getOrElse {
        return input
    }

    return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
}
