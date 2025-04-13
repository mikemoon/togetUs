package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomTimeBinding

@AndroidEntryPoint
class BottomTimeDialog : BottomBaseDialog<DialogBottomTimeBinding, BottomTimeViewModel>(){
    override val layoutId: Int
        get() = R.layout.dialog_bottom_time
    override val viewModel: BottomTimeViewModel by viewModels()
}