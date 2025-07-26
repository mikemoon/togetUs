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

    override fun initObserver() {
        super.initObserver()
    }
}