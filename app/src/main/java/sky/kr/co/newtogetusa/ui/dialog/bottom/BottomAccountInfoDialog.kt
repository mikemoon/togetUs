package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomAccountInfoBinding

@AndroidEntryPoint
class BottomAccountInfoDialog : BottomBaseDialog<DialogBottomAccountInfoBinding, BottomAccountInfoViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_account_info
    override val viewModel: BottomAccountInfoViewModel by viewModels()

    var bankName: String = ""
    var bankCode: String = ""
    var accountNumber: String = ""
    var onResetClick: (() -> Unit)? = null
    var onConfirmClick: (() -> Unit)? = null

    override fun init() {
        super.init()
        dataBinding.tvBankName.text = bankName.ifBlank { "은행" }
        dataBinding.tvAccountNumber.text = formatAccountNumber(accountNumber)
        dataBinding.btnReset.setOnClickListener {
            dismissAllowingStateLoss()
            onResetClick?.invoke()
        }
        dataBinding.btnConfirm.setOnClickListener {
            dismissAllowingStateLoss()
            onConfirmClick?.invoke()
        }
    }

    private fun formatAccountNumber(number: String): String {
        val digits = number.filter { it.isDigit() }
        if (digits.isBlank()) return number

        val pattern = when (bankCode) {
            "002" -> listOf(3, 2, 4, 2)
            "003" -> listOf(3, 6, 2)
            "004" -> if (digits.length >= 12) listOf(6, 2, 6) else listOf(3, 2, 6)
            "007" -> listOf(3, 4, 5)
            "011" -> if (digits.length >= 13) listOf(3, 6, 5) else listOf(3, 4, 6)
            "012" -> listOf(3, 4, 6)
            "020" -> listOf(4, 3, 6)
            "023" -> listOf(3, 2, 6)
            "027" -> listOf(3, 6, 3)
            "031" -> listOf(3, 2, 6, 1)
            "032" -> listOf(3, 4, 2, 4)
            "034" -> listOf(3, 3, 6)
            "035" -> listOf(3, 2, 7)
            "037" -> listOf(3, 2, 6)
            "039" -> listOf(3, 4, 5)
            "045", "048", "050", "092" -> listOf(4, 4, 6)
            "054" -> listOf(3, 6, 3)
            "064" -> listOf(3, 4, 6)
            "071" -> listOf(5, 2, 6)
            "081" -> listOf(3, 6, 5)
            "088" -> if (digits.length >= 14) listOf(3, 6, 6) else listOf(3, 3, 6)
            "089" -> listOf(3, 3, 6)
            "090" -> listOf(4, 2, 7)
            else -> if (digits.length >= 10) listOf(3, 4, 5) else emptyList()
        }

        if (pattern.isEmpty()) return digits

        var offset = 0
        val parts = mutableListOf<String>()
        pattern.forEach { length ->
            if (offset >= digits.length) return@forEach
            val end = (offset + length).coerceAtMost(digits.length)
            parts.add(digits.substring(offset, end))
            offset = end
        }
        if (offset < digits.length) {
            parts.add(digits.substring(offset))
        }
        return parts.joinToString("-")
    }
}
