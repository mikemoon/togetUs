package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomChatMoreBinding

@AndroidEntryPoint
class BottomChatMoreDialog : BottomBaseDialog<DialogBottomChatMoreBinding, BottomChatMoreViewModel>() {
    override val viewModel: BottomChatMoreViewModel by viewModels()
    override val layoutId: Int
        get() = R.layout.dialog_bottom_chat_more

    override fun init() {
        super.init()

    }
}