package sky.kr.co.newtogetusa.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * 닉네임 허용 문자: 한글(가-힣), 영문, 숫자
 * 허용 외 문자는 즉시 제거
 */
class AllowedNicknameFilter : InputFilter {
    private val blockPattern = Regex("[^가-힣A-Za-z0-9]")

    override fun filter(
        source: CharSequence, start: Int, end: Int,
        dest: Spanned, dstart: Int, dend: Int
    ): CharSequence? {
        val sub = source.subSequence(start, end).toString()
        val filtered = sub.replace(blockPattern, "")
        // null을 반환하면 원본이 그대로 적용됨. 바뀌었으면 바뀐 문자열 반환
        return if (filtered == sub) null else filtered
    }
}