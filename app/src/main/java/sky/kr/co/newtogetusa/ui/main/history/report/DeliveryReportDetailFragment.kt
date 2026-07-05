package sky.kr.co.newtogetusa.ui.main.history.report

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class DeliveryReportDetailFragment : androidx.fragment.app.Fragment() {

    private val viewModel: DeliveryReportDetailViewModel by viewModels()
    private val args: DeliveryReportDetailFragmentArgs by navArgs()

    private lateinit var contentEditText: AppCompatEditText
    private lateinit var countTextView: AppCompatTextView
    private lateinit var submitButton: AppCompatTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ConstraintLayout(requireContext()).apply {
            id = View.generateViewId()
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            addView(createTopBar())
            addView(createContentContainer())
            addView(createSubmitButton())
            setupKeyboardInsets(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onTextChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        submitButton.setOnClickListener {
            if (!submitButton.isEnabled) return@setOnClickListener
            hideKeyboard()
            MessageDialog.newInstance(
                msgTitle = "신고를 접수하겠어요?",
                msg = "${args.reason} 사유로 신고 접수가 이루어져요.",
                leftBtn = "취소",
                rightBtn = "접수하기"
            ).onRightBtn {
                viewModel.report(
                    userId = args.userId,
                    reason = args.reason,
                    content = contentEditText.text?.toString().orEmpty()
                )
            }.show(childFragmentManager, "DeliveryReportConfirmDialog")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.inputTextCountFlow.collect { count ->
                        countTextView.text = count
                    }
                }
                launch {
                    viewModel.canSubmitFlow.collect { canSubmit ->
                        submitButton.isEnabled = canSubmit
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryReportDetailViewModel.Event.ReportCompleted -> {
                    (requireActivity() as MainActivity).selectMainTab(R.id.home)
                }
                DeliveryReportDetailViewModel.Event.ReportFailed -> {
                    requireContext().toast("신고 접수에 실패했습니다.")
                }
            }
        }
    }

    private fun createTopBar(): View {
        return FrameLayout(requireContext()).apply {
            id = R.id.clTop
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                56.dp()
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }

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

    private fun createContentContainer(): View {
        return LinearLayout(requireContext()).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp(), 0, 20.dp(), 0)
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                topToBottom = R.id.clTop
                bottomToTop = R.id.tvRequest
            }

            addView(createTitle())
            addView(createDescription("신고를 접수하면 차단과 함께 서로 상대의 게시글이 보이지 않고, 더 이상 채팅을 보낼 수 없어요."))
            addView(createDescription("({메뉴명} > {메뉴명} > {메뉴명}에서 차단을 취소할 수 있어요.) ").apply {
                updateLayoutParams<LinearLayout.LayoutParams> {
                    topMargin = 8.dp()
                }
            })
            addView(createEditText())
            addView(createCounter())
        }
    }

    private fun createTitle(): View {
        return AppCompatTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                82.dp()
            )
            gravity = Gravity.CENTER_VERTICAL
            text = args.reason
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
        }
    }

    private fun createDescription(message: String): View {
        return AppCompatTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = message
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black_40))
        }
    }

    private fun createEditText(): View {
        contentEditText = AppCompatEditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 28.dp()
            }
            minHeight = 160.dp()
            gravity = Gravity.TOP
            maxLines = 6
            setPadding(8.dp(), 8.dp(), 8.dp(), 8.dp())
            hint = "신고 내용을 입력해 주세요."
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
            setHintTextColor(ContextCompat.getColor(requireContext(), R.color.black_40))
            setBackgroundResource(R.drawable.background_st_b10_s_w_r4)
        }
        return contentEditText
    }

    private fun createCounter(): View {
        countTextView = AppCompatTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8.dp()
            }
            gravity = Gravity.END
            text = "0/300"
            textSize = 10f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black_40))
        }
        return countTextView
    }

    private fun createSubmitButton(): View {
        submitButton = AppCompatTextView(requireContext()).apply {
            id = R.id.tvRequest
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                58.dp()
            ).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                marginStart = 20.dp()
                marginEnd = 20.dp()
                bottomMargin = 20.dp()
            }
            gravity = Gravity.CENTER
            text = "접수하기"
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_wopa40_wopa100))
            setBackgroundResource(R.drawable.selector_b20_p100_r4)
            isEnabled = false
        }
        return submitButton
    }

    private fun setupKeyboardInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val keyboardBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            submitButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = if (keyboardBottom > 0) keyboardBottom + 12.dp() else 20.dp()
            }
            insets
        }
    }

    private fun hideKeyboard() {
        requireContext().getSystemService<InputMethodManager>()
            ?.hideSoftInputFromWindow(contentEditText.windowToken, 0)
        contentEditText.clearFocus()
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}
