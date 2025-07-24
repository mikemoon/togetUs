package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomBankBinding
import sky.kr.co.newtogetusa.ui.base.BaseDialogFragment

@AndroidEntryPoint
class BottomBankDialog : BottomBaseDialog<DialogBottomBankBinding, BottomBankViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_bank
    override val viewModel: BottomBankViewModel by viewModels()
}