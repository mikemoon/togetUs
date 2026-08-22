package sky.kr.co.newtogetusa.ui.main.history.review

import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryReviewBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class DeliveryReviewFragment : BaseFragment<FragmentDeliveryReviewBinding, DeliveryReviewViewModel>() {

    override val layoutId: Int = R.layout.fragment_delivery_review
    override val viewModel: DeliveryReviewViewModel by viewModels()
    private val args: DeliveryReviewFragmentArgs by navArgs()
    private val reasonButtons = mutableMapOf<String, AppCompatTextView>()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        viewModel.load(args.deliveryId, args.isPlayer)
        bindStars()
        dataBinding.btnSubmit.setOnClickListener {
            viewModel.submit(args.deliveryId, args.isPlayer, dataBinding.etReview.text?.toString().orEmpty())
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.titleFlow.collect { dataBinding.tvTitle.text = it } }
                launch { viewModel.scoreFlow.collect { updateStars(it) } }
                launch { viewModel.reasonOptions.collect { bindReasons(it) } }
                launch { viewModel.selectedCodes.collect { updateReasonSelection(it) } }
                launch {
                    viewModel.canSubmitFlow.collect { enabled ->
                        dataBinding.btnSubmit.isEnabled = enabled
                        dataBinding.btnSubmit.alpha = if (enabled) 1f else 0.4f
                    }
                }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryReviewViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryReviewViewModel.Event.Invalid -> requireContext().toast("별점을 선택해 주세요.")
                DeliveryReviewViewModel.Event.SubmitFailed -> requireContext().toast("후기 등록에 실패했습니다.")
                DeliveryReviewViewModel.Event.SubmitSuccess -> {
                    requireContext().toast("후기가 등록되었습니다.")
                    findNavController().navigate(
                        DeliveryReviewFragmentDirections.actionDeliveryReviewFragmentToReceivedReviewFragment(
                            args.deliveryId,
                            args.isPlayer
                        )
                    )
                }
                DeliveryReviewViewModel.Event.LoadFailed -> Unit
                is DeliveryReviewViewModel.Event.ReceivedReviewNotFound -> Unit
                is DeliveryReviewViewModel.Event.ReviewBlocked -> requireContext().toast(event.message)
            }
        }
    }

    private fun bindStars() {
        listOf(dataBinding.tvStar1, dataBinding.tvStar2, dataBinding.tvStar3, dataBinding.tvStar4, dataBinding.tvStar5)
            .forEachIndexed { index, textView -> textView.setOnClickListener { viewModel.setScore(index + 1) } }
    }

    private fun updateStars(score: Int) {
        val selected = resources.getColor(R.color.primary_100, null)
        val normal = resources.getColor(R.color.black_10, null)
        listOf(dataBinding.tvStar1, dataBinding.tvStar2, dataBinding.tvStar3, dataBinding.tvStar4, dataBinding.tvStar5)
            .forEachIndexed { index, textView -> textView.setTextColor(if (index < score) selected else normal) }
    }

    private fun bindReasons(items: List<Pair<String, String>>) {
        dataBinding.llReasonOptions.removeAllViews()
        reasonButtons.clear()
        items.forEachIndexed { index, (code, name) ->
            val button = AppCompatTextView(requireContext()).apply {
                text = name
                gravity = android.view.Gravity.CENTER
                setTextAppearance(R.style.Paragraph17R)
                background = resources.getDrawable(R.drawable.selector_review_option_bg, null)
                setTextColor(ColorStateList.valueOf(resources.getColor(R.color.black_80, null)))
                setOnClickListener { viewModel.toggleCode(code) }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    56.dpToPx()
                ).apply {
                    if (index > 0) topMargin = 12.dpToPx()
                }
            }
            reasonButtons[code] = button
            dataBinding.llReasonOptions.addView(button)
        }
        updateReasonSelection(viewModel.selectedCodes.value)
    }

    private fun updateReasonSelection(selectedCodes: Set<String>) {
        val selectedColor = resources.getColor(R.color.primary_100, null)
        val normalColor = resources.getColor(R.color.black_80, null)
        reasonButtons.forEach { (code, button) ->
            val isSelected = selectedCodes.contains(code)
            button.isSelected = isSelected
            button.setTextColor(if (isSelected) selectedColor else normalColor)
        }
    }
}
