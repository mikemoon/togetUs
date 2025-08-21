package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomDeliveryFilterBinding

@AndroidEntryPoint
class BottomDeliveryFilterDialog : BottomBaseDialog<DialogBottomDeliveryFilterBinding, BottomDeliveryFilterViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_delivery_filter
    override val viewModel: BottomDeliveryFilterViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()
    }
}