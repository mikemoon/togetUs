package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomPaymentAgreeBinding

// iOS PaymentAgreePopupViewController 대응: 결제 동의 모달. 모든 약관 체크 시 '모두 동의' 활성
@AndroidEntryPoint
class BottomPaymentAgreeDialog :
    BottomBaseDialog<DialogBottomPaymentAgreeBinding, BottomPaymentAgreeViewModel>() {

    override val layoutId: Int = R.layout.dialog_bottom_payment_agree
    override val viewModel: BottomPaymentAgreeViewModel by viewModels()

    var onAgreeAll: (() -> Unit)? = null

    override fun initObserver() {
        super.initObserver()

        val checkBoxes = listOf(dataBinding.cbTerm1, dataBinding.cbTerm2, dataBinding.cbTerm3)
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                dataBinding.tvAgreeAll.isEnabled = checkBoxes.all { it.isChecked }
            }
        }

        dataBinding.tvCancel.setOnClickListener { dismissAllowingStateLoss() }
        dataBinding.tvAgreeAll.setOnClickListener {
            dismissAllowingStateLoss()
            onAgreeAll?.invoke()
        }
    }
}
