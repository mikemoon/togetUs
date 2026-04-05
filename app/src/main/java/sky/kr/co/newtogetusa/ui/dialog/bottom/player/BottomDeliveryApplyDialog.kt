package sky.kr.co.newtogetusa.ui.dialog.bottom.player

import androidx.fragment.app.viewModels
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomDeliveryApplyBinding
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomBaseDialog

class BottomDeliveryApplyDialog : BottomBaseDialog<DialogBottomDeliveryApplyBinding, BottomDeliveryApplyVM>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_delivery_apply
    override val viewModel: BottomDeliveryApplyVM by viewModels()
}