package sky.kr.co.newtogetusa.ui.main.history.review

import android.view.View
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
import sky.kr.co.newtogetusa.databinding.FragmentSentReviewBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class SentReviewFragment : BaseFragment<FragmentSentReviewBinding, DeliveryReviewViewModel>() {
    override val layoutId: Int = R.layout.fragment_sent_review
    override val viewModel: DeliveryReviewViewModel by viewModels()
    private val args: SentReviewFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        viewModel.loadSentReview(args.deliveryId, args.isPlayer)
        dataBinding.btnReceivedReview.setOnClickListener {
            findNavController().navigate(
                SentReviewFragmentDirections.actionSentReviewFragmentToReceivedReviewFragment(
                    args.deliveryId,
                    args.isPlayer
                )
            )
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sentReviewTitleFlow.collect { title ->
                        dataBinding.tvTitle.text = title
                    }
                }
                launch {
                    viewModel.sentReview.collect { review ->
                        if (review == null) return@collect
                        dataBinding.tvStars.text = "★".repeat(review.stars.coerceIn(0, 5))
                        dataBinding.tvContents.text = review.contents.orEmpty()
                        dataBinding.tvContents.visibility =
                            if (review.contents.isNullOrBlank()) View.GONE else View.VISIBLE
                        bindTags(review.items)
                    }
                }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryReviewViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryReviewViewModel.Event.LoadFailed -> requireContext().toast("보낸 후기를 불러오지 못했습니다.")
                else -> Unit
            }
        }
    }

    private fun bindTags(items: List<String>) = with(dataBinding.llTags) {
        removeAllViews()
        visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        items.forEachIndexed { index, item ->
            addView(AppCompatTextView(requireContext()).apply {
                text = item
                gravity = android.view.Gravity.CENTER
                setTextAppearance(R.style.Paragraph14R)
                setTextColor(resources.getColor(R.color.primary_100, null))
                setBackgroundResource(R.drawable.background_st_p60_s_p10_r4)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    36.dpToPx()
                ).apply {
                    if (index > 0) topMargin = 8.dpToPx()
                }
            })
        }
    }
}
