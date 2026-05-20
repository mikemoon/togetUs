package sky.kr.co.newtogetusa.ui.main.history.report

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentReportResultBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ReportResultFragment : BaseFragment<FragmentReportResultBinding, ReportResultVM>() {

    override val layoutId: Int = R.layout.fragment_report_result
    override val viewModel: ReportResultVM by viewModels()

    private val args: ReportResultFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel

        bindContent()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                ReportResultVM.Event.Back -> findNavController().popBackStack()
                ReportResultVM.Event.OpenReceivedReview -> {
                    if (args.deliveryId > 0L) {
                        findNavController().navigate(
                            ReportResultFragmentDirections.actionReportResultFragmentToReceivedReviewFragment(
                                args.deliveryId,
                                args.isPlayer
                            )
                        )
                    }
                }
            }
        }
    }

    private fun bindContent() {
        dataBinding.tvTitle.text = "홍길동님에게 거래 후기를 남겼어요."

        dataBinding.tvMessage.isVisible = !args.message.isNullOrBlank()
        dataBinding.tvMessage.text = args.message

        dataBinding.btnReceivedReview.isVisible = args.showBottomButton && args.deliveryId > 0L

        dataBinding.cgReasons.removeAllViews()
        args.reasons.orEmpty().forEach { reason ->
            val chip = Chip(requireContext()).apply {
                text = reason
                isClickable = false
                isCheckable = false
                setEnsureMinTouchTargetSize(false)
                setChipBackgroundColorResource(R.color.black_5)
                setTextColor(resources.getColor(R.color.black_80, null))
                chipCornerRadius = 4f * resources.displayMetrics.density
            }
            dataBinding.cgReasons.addView(chip)
        }
    }
}
