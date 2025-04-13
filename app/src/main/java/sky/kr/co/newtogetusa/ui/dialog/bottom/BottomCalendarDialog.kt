package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomCalendarBinding

@AndroidEntryPoint
class BottomCalendarDialog : BottomBaseDialog<DialogBottomCalendarBinding, BottomCalendarViewModel>()  {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_calendar
    override val viewModel: BottomCalendarViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()
    }
}