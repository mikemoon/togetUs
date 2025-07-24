package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.widget.NumberPicker
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomDateBinding
import sky.kr.co.newtogetusa.ui.base.BaseDialogFragment
import java.util.Calendar

@AndroidEntryPoint
class BottomDateDialog : BaseDialogFragment<DialogBottomDateBinding, BottomDateViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_date
    override val viewModel: BottomDateViewModel by viewModels()

    override fun init() {
        super.init()

        // 시작: 현재로부터 2년 전, 끝: 현재
        val calendar = Calendar.getInstance()
        val end = calendar.clone() as Calendar
        val start = calendar.clone() as Calendar
        start.add(Calendar.YEAR, -2) // 2년 전으로 이동

        val months = mutableListOf<String>()
        val temp = start.clone() as Calendar
        while (!temp.after(end)) {
            val year = temp.get(Calendar.YEAR)
            val month = temp.get(Calendar.MONTH) + 1
            months.add("${year}년 ${month}월")
            temp.add(Calendar.MONTH, 1)
        }

        dataBinding.monthPicker.apply {
            minValue = 0
            maxValue = months.size - 1
            displayedValues = months.toTypedArray()
            wrapSelectorWheel = false
            value = months.size - 1 //초기위치 현재월
        }
    }

}