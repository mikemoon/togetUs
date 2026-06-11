package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomProfileMoreBinding

@AndroidEntryPoint
class BottomProfileMoreDialog : BottomBaseDialog<DialogBottomProfileMoreBinding, BottomProfileMoreViewModel>() {
    override val viewModel: BottomProfileMoreViewModel by viewModels()
    override val layoutId: Int
        get() = R.layout.dialog_bottom_profile_more

    var reportAction = {}
    var blockAction = {}

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                BottomProfileMoreViewModel.Event.Block -> {
                    blockAction.invoke()
                    dismissAllowingStateLoss()
                }
                BottomProfileMoreViewModel.Event.Report -> {
                    reportAction.invoke()
                    dismissAllowingStateLoss()
                }
            }
        }
    }
}
