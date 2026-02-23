package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomMoreBinding

@AndroidEntryPoint
class BottomMoreDialog : BottomBaseDialog<DialogBottomMoreBinding, BottomMoreVM>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_more
    override val viewModel: BottomMoreVM by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                BottomMoreVM.Event.Cancel -> {
                    parentFragmentManager.setFragmentResult(
                        "BottomMoreResult",
                        bundleOf("action" to "Cancel")
                    )
                    dismiss()
                }
                BottomMoreVM.Event.Modify -> {
                    parentFragmentManager.setFragmentResult(
                        "BottomMoreResult",
                        bundleOf("action" to "Modify")
                    )
                    dismiss()
                }
                BottomMoreVM.Event.Chatting -> {
                    parentFragmentManager.setFragmentResult(
                        "BottomMoreResult",
                        bundleOf("action" to "Chatting")
                    )
                    dismiss()
                }
            }
        }
    }

}