package sky.kr.co.newtogetusa.ui.main.history.player

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHistoryDeliveryDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.player.BottomDeliveryApplyDialog
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class PlayerHistoryDetailFragment :
    BaseFragment<FragmentHistoryDeliveryDetailBinding, PlayerHistoryDetailViewModel>() {

    override val layoutId: Int = R.layout.fragment_history_delivery_detail
    override val viewModel: PlayerHistoryDetailViewModel by viewModels()

    private val args: PlayerHistoryDetailFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel

        viewModel.getDeliveryDetailInfo(args.deliveryId)

        dataBinding.ivBack.setOnClickListener {
            viewModel.onBackClick()
        }

        dataBinding.tvBottomSecondaryButton.setOnClickListener {
            viewModel.onSecondaryClick()
        }

        dataBinding.tvBottomPrimaryButton.setOnClickListener {
            viewModel.onPrimaryClick()
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.buttonState.collectLatest { buttonState ->
                        if (buttonState == PlayerHistoryDetailViewModel.ButtonState.Hidden) {
                            dataBinding.llBottomButtonContainer.visibility = View.GONE
                            return@collectLatest
                        }

                        dataBinding.llBottomButtonContainer.visibility = View.VISIBLE
                        dataBinding.tvBottomPrimaryButton.text = buttonState.primaryText

                        if (buttonState.showSecondary) {
                            dataBinding.tvBottomSecondaryButton.visibility = View.VISIBLE
                            dataBinding.tvBottomSecondaryButton.text = buttonState.secondaryText
                        } else {
                            dataBinding.tvBottomSecondaryButton.visibility = View.GONE
                        }

                        if (buttonState == PlayerHistoryDetailViewModel.ButtonState.Done) {
                            dataBinding.tvBottomPrimaryButton.setBackgroundResource(R.drawable.background_s_b5_r4)
                            dataBinding.tvBottomPrimaryButton.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.black_60)
                            )
                        } else {
                            dataBinding.tvBottomPrimaryButton.setBackgroundResource(R.drawable.background_s_p100_r4)
                            dataBinding.tvBottomPrimaryButton.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        }
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PlayerHistoryDetailViewModel.Event.Back -> findNavController().popBackStack()
                is PlayerHistoryDetailViewModel.Event.ActionSuccess -> requireContext().toast(event.msg)
                is PlayerHistoryDetailViewModel.Event.ActionFail -> requireContext().toast("요청 처리에 실패했어요.")
                is PlayerHistoryDetailViewModel.Event.ShowApplyDialog -> {
                    val nickname = viewModel.deliveryDetail.value?.requester_rating?.nickname.orEmpty()
                    BottomDeliveryApplyDialog().apply {
                        requesterNickname = nickname
                        confirmCallback = { this@PlayerHistoryDetailFragment.viewModel.confirmApply() }
                        noticeCallback = {
                            dismissAllowingStateLoss()
                            findNavController().navigate(
                                PlayerHistoryDetailFragmentDirections.actionPlayerHistoryDetailFragmentToPlayerDeliveryNoticeFragment()
                            )
                        }
                    }.show(childFragmentManager, "BottomDeliveryApplyDialog")
                }
                is PlayerHistoryDetailViewModel.Event.Chat -> requireContext().toast("채팅 기능을 준비중입니다.")
                is PlayerHistoryDetailViewModel.Event.DoneInfo -> requireContext().toast("이미 완료된 요청입니다.")
                is PlayerHistoryDetailViewModel.Event.OpenReport -> {
                    findNavController().navigate(
                        PlayerHistoryDetailFragmentDirections.actionPlayerHistoryDetailFragmentToReportFragment()
                    )
                }
            }
        }
    }
}