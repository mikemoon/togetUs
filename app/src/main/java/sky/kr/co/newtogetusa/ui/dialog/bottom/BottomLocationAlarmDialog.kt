package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomLocationAlarmBinding

@AndroidEntryPoint
class BottomLocationAlarmDialog :
    BottomBaseDialog<DialogBottomLocationAlarmBinding, BottomLocationAlarmViewModel>() {

    override val layoutId: Int
        get() = R.layout.dialog_bottom_location_alarm
    override val viewModel: BottomLocationAlarmViewModel by viewModels()

    var isOn: Boolean = true
    var onSettingClick: (() -> Unit)? = null

    override fun init() {
        super.init()
        val title = if (isOn) "현위치 동행 알림 ON" else "현위치 동행 알림 OFF"
        val message = if (isOn) {
            "스위치를 켜면 내 위치(5분 주기)에서 현재 설정된 도착 가능지역 방면의 동행요청을 추가 수신합니다.\n\n※ 앱을 종료해도 마지막 위치를 기준으로 알림을 계속 받습니다."
        } else {
            "현위치 기반 매칭을 중단합니다.\n\n지정하신 기본 배송 범위 내에서만 동행요청을 수신합니다."
        }
        val descBackground = if (isOn) {
            R.drawable.background_s_primary5_r8
        } else {
            R.drawable.background_s_b2_r8
        }

        dataBinding.tvTitle.text = title
        dataBinding.tvDescription.text = message
        dataBinding.layoutDescription.background =
            ContextCompat.getDrawable(requireContext(), descBackground)

        dataBinding.tvClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
        dataBinding.tvSetting.setOnClickListener {
            dismissAllowingStateLoss()
            onSettingClick?.invoke()
        }
    }
}
