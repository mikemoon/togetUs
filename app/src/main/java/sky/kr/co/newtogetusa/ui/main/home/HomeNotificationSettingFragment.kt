package sky.kr.co.newtogetusa.ui.main.home

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHomeNotificationSettingBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.my.MySettingViewModel

@AndroidEntryPoint
class HomeNotificationSettingFragment :
    BaseFragment<FragmentHomeNotificationSettingBinding, MySettingViewModel>() {
    override val layoutId: Int = R.layout.fragment_home_notification_setting
    override val viewModel: MySettingViewModel by viewModels()
    private var bindingSettings = false

    override fun init() {
        super.init()
        dataBinding.ivBack.setOnClickListener { findNavController().popBackStack() }
        dataBinding.swPush.setOnCheckedChangeListener { _, isChecked ->
            if (bindingSettings) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(pushYn = isChecked) }
        }
        dataBinding.swChat.setOnCheckedChangeListener { _, isChecked ->
            if (bindingSettings) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(chatYn = isChecked) }
        }
        dataBinding.swDelivery.setOnCheckedChangeListener { _, isChecked ->
            if (bindingSettings) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(deliveryYn = isChecked) }
        }
        dataBinding.swMarketing.setOnCheckedChangeListener { _, isChecked ->
            if (bindingSettings) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(marketingYn = isChecked) }
        }
        dataBinding.swNight.setOnCheckedChangeListener { _, isChecked ->
            if (bindingSettings) return@setOnCheckedChangeListener
            viewModel.updateAlarmSetting { it.copy(nightPushYn = isChecked) }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alarmSettingState.collect { setting ->
                    if (setting == null) return@collect
                    bindingSettings = true
                    dataBinding.swPush.setCheckedSilently(setting.pushYn)
                    dataBinding.swChat.setCheckedSilently(setting.chatYn)
                    dataBinding.swDelivery.setCheckedSilently(setting.deliveryYn)
                    dataBinding.swMarketing.setCheckedSilently(setting.marketingYn)
                    dataBinding.swNight.setCheckedSilently(setting.nightPushYn)
                    bindingSettings = false
                }
            }
        }
    }

    private fun androidx.appcompat.widget.SwitchCompat.setCheckedSilently(value: Boolean) {
        if (isChecked != value) isChecked = value
    }
}
