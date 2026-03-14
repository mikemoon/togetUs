package sky.kr.co.newtogetusa.ui.main.my

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentMySettingBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.login.LoginActivity
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MySettingFragment : BaseFragment<FragmentMySettingBinding, MySettingViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_my_setting
    override val viewModel: MySettingViewModel by viewModels()

    private val args : MySettingFragmentArgs by navArgs()
    private var isSwitchBindingInProgress = false

    override fun initObserver() {
        super.initObserver()

        bindAlarmSwitchListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.alarmSettingState.collect{ setting ->
                    setting ?: return@collect
                    Timber.d("alarmSettingState $setting")
                    isSwitchBindingInProgress = true
                    dataBinding.swDelivery.isChecked = setting.deliveryYn
                    dataBinding.swChatting.isChecked = setting.chatYn
                    dataBinding.swMarketing.isChecked = setting.marketingYn
                    dataBinding.swNight.isChecked = setting.nightPushYn
                    isSwitchBindingInProgress = false
                }
            }
        }


        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                MySettingViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                MySettingViewModel.Event.ManageProfile ->{
                    findNavController().navigate(
                        MySettingFragmentDirections.actionMySettingFragmentToProfileManageSubFragment(
                            profileDto = args.profileDto,
                        )
                    )
                }
                MySettingViewModel.Event.DeliveryAlarm ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "배송알림",
                            msg = "배송 알림을 끄면 배송 관련 정보를 확인하기 어려울 수 있어요. (단, 배송이 시작되면 알림 설정과 무관하게 발송됩니다.)",
                            rightBtn = "알림끄기",
                            leftBtn = "취소",
                        )
                    )
                }
                MySettingViewModel.Event.ChattingAlarm ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "채팅알림",
                            msg = "채팅 알림을 끄면 유저/플레이어와의 소통에 어려움이 있을 수 있어요.",
                            rightBtn = "알림끄기",
                            leftBtn = "취소",
                        )
                    )

                }
                MySettingViewModel.Event.MarkettingAlarm ->{
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
                    val formatted = current.format(formatter)
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "마켓팅정보 앱 푸시 알림 동의 안내",
                            msg = "전송자: 투겟어스\n" +
                                    "수신동의 일시: ${formatted}\n" +
                                    "처리내용: 수신동의 처리완료",
                            rightBtn = "확인",
                        )
                    )
                }
                MySettingViewModel.Event.NightAlarm ->{

                }
                MySettingViewModel.Event.Logout ->{
                    MessageDialog.newInstance(
                        msgTitle = "로그아웃하시겠어요?",
                        msg = "서비스를 이용하려면 다시 로그인하셔야 해요",
                        rightBtn = "로그아웃",
                        leftBtn = "취소"
                    ).onRightBtn {
                        viewModel.logout {
                            requireContext().startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().finish()
                        }
                    }.show(childFragmentManager, "")
                }
                MySettingViewModel.Event.WithDraw ->{
                    findNavController().navigate(R.id.action_mySettingFragment_to_withDrawFragment)
                }
            }
        }
    }

    private fun bindAlarmSwitchListeners() {
        dataBinding.swDelivery.setOnCheckedChangeListener { _, isChecked ->
            if (isSwitchBindingInProgress) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(deliveryYn = isChecked) }
        }

        dataBinding.swChatting.setOnCheckedChangeListener { _, isChecked ->
            if (isSwitchBindingInProgress) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(chatYn = isChecked) }
        }

        dataBinding.swMarketing.setOnCheckedChangeListener { _, isChecked ->
            if (isSwitchBindingInProgress) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(marketingYn = isChecked) }
        }

        dataBinding.swNight.setOnCheckedChangeListener { _, isChecked ->
            if (isSwitchBindingInProgress) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(nightPushYn = isChecked) }
        }
    }

}