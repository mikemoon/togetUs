package sky.kr.co.newtogetusa.ui.main.history.report

import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentReportBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ReportFragment : BaseFragment<FragmentReportBinding, ReportVM>() {

    override val layoutId: Int = R.layout.fragment_report
    override val viewModel: ReportVM by viewModels()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel

        bindStars()
        bindReasonButtons()
        dataBinding.btnSubmit.setOnClickListener {
            viewModel.onSubmit(dataBinding.etReview.text?.toString().orEmpty())
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scoreFlow.collect { score ->
                        updateStarState(score)
                    }
                }

                launch {
                    viewModel.selectedReasonsFlow.collect { selectedReasons ->
                        dataBinding.groupReason.children.forEach { view ->
                            val reason = view.tag as? String ?: return@forEach
                            view.isSelected = selectedReasons.contains(reason)
                        }
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                ReportVM.Event.Back -> findNavController().popBackStack()
                is ReportVM.Event.OpenResult -> {
                    findNavController().navigate(
                        ReportFragmentDirections.actionReportFragmentToReportResultFragment(
                            reasons = event.reasons.toTypedArray(),
                            message = event.message,
                            showBottomButton = event.showBottomButton,
                        )
                    )
                }
            }
        }
    }

    private fun bindStars() {
        val stars = listOf(
            dataBinding.tvStar1,
            dataBinding.tvStar2,
            dataBinding.tvStar3,
            dataBinding.tvStar4,
            dataBinding.tvStar5,
        )
        stars.forEachIndexed { index, textView ->
            textView.setOnClickListener { viewModel.setScore(index + 1) }
        }
    }

    private fun updateStarState(score: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.primary_100)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.black_10)
        val stars = listOf(
            dataBinding.tvStar1,
            dataBinding.tvStar2,
            dataBinding.tvStar3,
            dataBinding.tvStar4,
            dataBinding.tvStar5,
        )

        stars.forEachIndexed { index, textView ->
            textView.setTextColor(if (index < score) selectedColor else defaultColor)
        }
    }

    private fun bindReasonButtons() {
        dataBinding.btnReasonTime.tag = dataBinding.btnReasonTime.text.toString()
        dataBinding.btnReasonFast.tag = dataBinding.btnReasonFast.text.toString()
        dataBinding.btnReasonManner.tag = dataBinding.btnReasonManner.text.toString()
        dataBinding.btnReasonProduct.tag = dataBinding.btnReasonProduct.text.toString()

        listOf(
            dataBinding.btnReasonTime,
            dataBinding.btnReasonFast,
            dataBinding.btnReasonManner,
            dataBinding.btnReasonProduct,
        ).forEach { button ->
            button.setOnClickListener {
                val reason = button.tag as? String ?: return@setOnClickListener
                viewModel.toggleReason(reason)
            }
        }
    }
}