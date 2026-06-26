package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.NumberPicker
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomTimeBinding

@AndroidEntryPoint
class BottomTimeDialog : BottomBaseDialog<DialogBottomTimeBinding, BottomTimeViewModel>(){
    override val layoutId: Int
        get() = R.layout.dialog_bottom_time
    override val viewModel: BottomTimeViewModel by viewModels()

    var timeCallback : ((Int, Int) -> Unit)? = null
    var allowNegotiable: Boolean = true

    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun initObserver() {
        super.initObserver()

        dataBinding.clTimeNegotiable.isVisible = allowNegotiable
        if (!allowNegotiable) {
            dataBinding.switchTime.isChecked = false
        }

        dataBinding.switchTime.setOnCheckedChangeListener { _, isChecked ->
            dataBinding.llTimePicker.isVisible = !isChecked
        }

        dataBinding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedHour = hourOfDay
            selectedMinute = minute
        }

        dataBinding.tvConfirm.setOnClickListener {
            if (dataBinding.switchTime.isChecked) {
                timeCallback?.invoke(-1, -1) // 시간 협의
            } else {
                deliverSelectedTime()
            }
            dismissAllowingStateLoss()
        }
    }

    private fun deliverSelectedTime() {
        val timePicker = dataBinding.timePicker

        val hour = if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePicker.hour
        } else {
            timePicker.currentHour
        }

        val minute = if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePicker.minute
        } else {
            timePicker.currentMinute
        }

        // 오전/오후 판별
        val isPM = if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePicker.hour >= 12 && timePicker.is24HourView.not()
        } else {
            timePicker.currentHour >= 12 && timePicker.is24HourView.not()
        }

        // 👉 12시간제 → 24시간제로 변환
        val hour24 = when {
            timePicker.is24HourView -> hour
            isPM && hour < 12 -> hour + 12
            !isPM && hour == 12 -> 0
            else -> hour
        }

        timeCallback?.invoke(hour24, minute)
    }

}
