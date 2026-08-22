package sky.kr.co.newtogetusa.ui.dialog.message

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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

    private var confirmAction: () -> Unit = {}
    private var customerCenterAction: () -> Unit = {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateConfirmEnabled(false)

        dataBinding.cbConfirm.setOnCheckedChangeListener { _, isChecked ->
            updateConfirmEnabled(isChecked)
        }

        dataBinding.tvConfirm.setOnClickListener {
            if (!dataBinding.cbConfirm.isChecked) return@setOnClickListener
            confirmAction.invoke()
            dismiss()
        }

        dataBinding.tvCustomerCenter.setOnClickListener {
            customerCenterAction.invoke()
            dismiss()
        }
    }

    fun onConfirm(action: () -> Unit): ReceiveConfirmDialog {
        confirmAction = action
        return this
    }

    fun onCustomerCenter(action: () -> Unit = {}): ReceiveConfirmDialog {
        customerCenterAction = action
        return this
    }

    private fun updateConfirmEnabled(enabled: Boolean) {
        dataBinding.tvConfirm.isEnabled = enabled
        dataBinding.tvConfirm.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (enabled) R.color.black_80 else R.color.black_20
            )
        )
    }
}
