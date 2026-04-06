package sky.kr.co.newtogetusa.ui.dialog.bottom.player

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomDeliveryApplyBinding
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomBaseDialog

@AndroidEntryPoint
class BottomDeliveryApplyDialog : BottomBaseDialog<DialogBottomDeliveryApplyBinding, BottomDeliveryApplyVM>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_delivery_apply
    override val viewModel: BottomDeliveryApplyVM by viewModels()

    var requesterNickname: String = ""
    var confirmCallback: (() -> Unit)? = null
    var noticeCallback: (() -> Unit)? = null

    override fun init() {
        super.init()

        dataBinding.tvTitleDesc.text = "${requesterNickname}님의 동행 요청에 지원하시겠어요?"
        dataBinding.tvDescription.text = "지원 시 ${requesterNickname}님의 최종 선택 후 거래가 확정되며, 거래 확정 알림이 발송되어요. 확인 필요 사항은 채팅으로 직접 문의해 보세요."
        dataBinding.ivClose.setOnClickListener { viewModel.onCloseClick() }
        dataBinding.tvApply.setOnClickListener { viewModel.onApplyClick() }
        dataBinding.cbAgree.setOnCheckedChangeListener { _, isChecked -> viewModel.onAgreeChanged(isChecked) }
        dataBinding.ivNoticeArrow.setOnClickListener {
            noticeCallback?.invoke()
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                BottomDeliveryApplyVM.Event.Close -> dismissAllowingStateLoss()
                BottomDeliveryApplyVM.Event.Confirm -> {
                    confirmCallback?.invoke()
                    dismissAllowingStateLoss()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.agreeWarning.collectLatest { isChecked ->
                    dataBinding.tvApply.isEnabled = isChecked
                }
            }
        }
    }
}