package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomChatMoreBinding
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class BottomChatMoreDialog : BottomBaseDialog<DialogBottomChatMoreBinding, BottomChatMoreViewModel>() {
    override val viewModel: BottomChatMoreViewModel by viewModels()
    override val layoutId: Int
        get() = R.layout.dialog_bottom_chat_more

    var reportAction = {}
    var alarmOnAction = {}
    var alarmOffAction = {}
    var blockAction = {}
    var exitAction = {}
    var isBlocked = false
    var isNotificationOn = true
    var isReported = false

    override fun init() {
        super.init()
        dataBinding.tvAlarm.text = getString(
            if (isNotificationOn) R.string.alarm_off else R.string.alarm_on
        )
        dataBinding.tvBlock.text = getString(R.string.get_block)
        dataBinding.tvBlock.isVisible = !isBlocked
        dataBinding.tvReport.isVisible = !isReported
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                BottomChatMoreViewModel.Event.AlarmOff -> {
                    if (isNotificationOn) {
                        alarmOffAction.invoke()
                    } else {
                        alarmOnAction.invoke()
                    }
                    dismissAllowingStateLoss()
                }
                BottomChatMoreViewModel.Event.Block -> {
                    dialogFragmentShow(
                        requireActivity().supportFragmentManager,
                        MessageDialog.newInstance(
                            "차단과 함께 서로 상대의 게시글이 보이지 않고, 더 이상 채팅을 보낼 수 없어요.",
                            rightBtn = "차단하기",
                            leftBtn = "취소",
                            msgTitle = "정말 차단하시겠어요?"
                        ).onRightBtn { blockAction.invoke() }
                    )
                    dismissAllowingStateLoss()
                }
                BottomChatMoreViewModel.Event.Report -> {
                    reportAction.invoke()
                    dismissAllowingStateLoss()
                }
                BottomChatMoreViewModel.Event.Exit -> {
                    dialogFragmentShow(
                        requireActivity().supportFragmentManager,
                        MessageDialog.newInstance(
                            msg = "채팅방을 나가시면 상대와의 채팅 내역을 다시 확인 할 수 없어요.",
                            msgTitle = "채팅방을 나가시겠어요?",
                            rightBtn = "나가기",
                            leftBtn = "취소",
                    ).onRightBtn { exitAction.invoke() })
                    dismissAllowingStateLoss()
                }
            }
        }
    }


}
