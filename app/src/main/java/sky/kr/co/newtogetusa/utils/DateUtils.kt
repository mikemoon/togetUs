package sky.kr.co.newtogetusa.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
