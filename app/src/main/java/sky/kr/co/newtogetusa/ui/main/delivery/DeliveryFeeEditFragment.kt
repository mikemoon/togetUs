package sky.kr.co.newtogetusa.ui.main.delivery

import android.view.View
import android.view.Gravity
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryFinalReq
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryFeeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading

@AndroidEntryPoint
class DeliveryFeeEditFragment : BaseFragment<FragmentDeliveryFeeBinding, DeliveryFeeVM>() {

    override val layoutId: Int = R.layout.fragment_delivery_fee
    override val viewModel: DeliveryFeeVM by viewModels()

    private val deliveryId: Long by lazy { arguments?.getLong("deliveryId") ?: -1L }
    private var hasAppliedInitialAdjustFee = false

    override fun init() {
        super.init()
        setupEditUi()
        setupBackCallback()

        if (deliveryId > 0L) {
            viewModel.loadDeliveryFeeForEdit(deliveryId)
        } else {
            handleBack()
        }
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.etAdjustFee.doAfterTextChanged {
            updateSubmitState()
            updateAdjustFeeClearVisibility()
        }

        dataBinding.ivAdjustFeeClear.setOnClickListener {
            dataBinding.etAdjustFee.text?.clear()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryUiModel.collect { uiModel ->
                    uiModel ?: return@collect
                    dataBinding.uiModel = uiModel

                    if (!hasAppliedInitialAdjustFee) {
                        dataBinding.etAdjustFee.setText(uiModel.adjustFee)
                        dataBinding.etAdjustFee.setSelection(dataBinding.etAdjustFee.text?.length ?: 0)
                        hasAppliedInitialAdjustFee = true
                        updateSubmitState()
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryFeeVM.Event.Back -> handleBack()
                DeliveryFeeVM.Event.RegisterDelivery -> submitEdit()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collect { show ->
                    if (show) showLoading() else hideLoading()
                }
            }
        }
    }

    private fun setupEditUi() = with(dataBinding) {
        llAgree.visibility = View.GONE
        llNotice.visibility = View.GONE
        llAdjustFeeInput.setBackgroundResource(R.drawable.background_st_p60_s_p10_r4)
        etAdjustFee.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        etAdjustFee.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        tvRegister.text = "동행 요청 수정"
        updateAdjustFeeClearVisibility()
        this@DeliveryFeeEditFragment.viewModel.setRegisterButtonEnabled(false)
    }

    private fun setupBackCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )
    }

    private fun updateSubmitState() = with(dataBinding) {
        val isValid = parseAdjustFeeOrNull()?.let { it >= 0L } == true
        this@DeliveryFeeEditFragment.viewModel.setRegisterButtonEnabled(isValid)
        llAdjustFeeInput.setBackgroundResource(
            if (isValid) R.drawable.background_st_p60_s_p10_r4
            else R.drawable.background_st_primary100_s_primary10_r4
        )
    }

    private fun updateAdjustFeeClearVisibility() = with(dataBinding) {
        ivAdjustFeeClear.visibility =
            if (etAdjustFee.text?.isNotEmpty() == true) View.VISIBLE else View.GONE
    }

    private fun submitEdit() {
        val adjustFee = parseAdjustFeeOrNull() ?: return
        viewModel.registerDelivery(DeliveryFinalReq(fee_adjust = adjustFee)) { success ->
            if (!success) return@registerDelivery

            MessageDialog.newInstance(
                msg = "수정되었습니다.",
                rightBtn = "확인"
            ).onRightBtn {
                handleBack()
            }.show(childFragmentManager, "MessageDialog")
        }
    }

    private fun parseAdjustFeeOrNull(): Long? =
        dataBinding.etAdjustFee.text?.toString()
            ?.replace(",", "")
            ?.trim()
            ?.toLongOrNull()

    private fun handleBack() {
        val navController = findNavController()
        navController.popBackStack()
    }
}
