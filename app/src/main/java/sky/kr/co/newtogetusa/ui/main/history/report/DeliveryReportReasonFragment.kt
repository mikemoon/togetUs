package sky.kr.co.newtogetusa.ui.main.history.report

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R

@AndroidEntryPoint
class DeliveryReportReasonFragment : androidx.fragment.app.Fragment() {

    private val args: DeliveryReportReasonFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

            addView(createTopBar())
            addView(createTitle())
            addView(createReasonList())
        }
    }

    private fun createTopBar(): View {
        return FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                56.dp()
            )

            addView(
                AppCompatImageView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(24.dp(), 24.dp()).apply {
                        gravity = Gravity.CENTER_VERTICAL or Gravity.START
                        marginStart = 20.dp()
                    }
                    setImageResource(R.drawable.left_l)
                    setOnClickListener { findNavController().popBackStack() }
                }
            )
        }
    }

    private fun createTitle(): View {
        return AppCompatTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                82.dp()
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20.dp(), 0, 20.dp(), 0)
            text = "신고 사유를 선택해 주세요."
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
        }
    }

    private fun createReasonList(): View {
        return RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ReasonAdapter(REASONS) { reason ->
                findNavController().navigate(
                    DeliveryReportReasonFragmentDirections
                        .actionDeliveryReportReasonFragmentToDeliveryReportDetailFragment(
                            userId = args.userId,
                            reason = reason.label
                        )
                )
            }
            addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                    ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let {
                        setDrawable(it)
                    }
                }
            )
        }
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()

    private data class ReportReason(val label: String)

    private class ReasonAdapter(
        private val items: List<ReportReason>,
        private val onClick: (ReportReason) -> Unit
    ) : RecyclerView.Adapter<ReasonAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                AppCompatTextView(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        60.dp(parent)
                    )
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(20.dp(parent), 0, 20.dp(parent), 0)
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(parent.context, R.color.black_80))
                }
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textView.text = item.label
            holder.textView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size

        private fun Int.dp(parent: ViewGroup): Int =
            (this * parent.resources.displayMetrics.density).toInt()

        class ViewHolder(val textView: AppCompatTextView) : RecyclerView.ViewHolder(textView)
    }

    companion object {
        private val REASONS = listOf(
            ReportReason("거래 중 분쟁"),
            ReportReason("욕설・비방・혐오"),
            ReportReason("음란・성적 행위"),
            ReportReason("사기・사칭"),
            ReportReason("스팸"),
            ReportReason("기타")
        )
    }
}
