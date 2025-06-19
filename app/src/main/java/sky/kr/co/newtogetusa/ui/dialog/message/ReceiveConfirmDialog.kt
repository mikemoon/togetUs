package sky.kr.co.newtogetusa.ui.dialog.message

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogReceiveConfirmBinding
import sky.kr.co.newtogetusa.ui.base.BaseDialogFragment

@AndroidEntryPoint
class ReceiveConfirmDialog : BaseDialogFragment<DialogReceiveConfirmBinding, ReceiveConfirmDialogViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_receive_confirm
    override val viewModel: ReceiveConfirmDialogViewModel by viewModels()
}