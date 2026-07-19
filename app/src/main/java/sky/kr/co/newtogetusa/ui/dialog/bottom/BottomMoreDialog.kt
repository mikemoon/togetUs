package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomMoreBinding

@AndroidEntryPoint
class BottomMoreDialog : BottomBaseDialog<DialogBottomMoreBinding, BottomMoreVM>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_more
    override val viewModel: BottomMoreVM by viewModels()

    override fun init() {
        super.init()
        val actionKeys = arguments?.getStringArray(ARG_ACTIONS).orEmpty()
        val actionViews = listOf(
            dataBinding.tvAction1,
            dataBinding.tvAction2,
            dataBinding.tvAction3
        )

        actionViews.forEachIndexed { index, view ->
            val action = actionKeys.getOrNull(index)
            view.isVisible = action != null
            if (action != null) {
                view.text = actionLabel(action)
                view.setOnClickListener {
                    parentFragmentManager.setFragmentResult(
                        "BottomMoreResult",
                        Bundle().apply { putString("action", action) }
                    )
                    dismiss()
                }
            }
        }
    }

    private fun actionLabel(action: String): String = when (action) {
        "Delete" -> "삭제하기"
        "Cancel" -> "취소하기"
        "Modify" -> "수정하기"
        "Chatting" -> "진행중인 채팅"
        else -> action
    }

    companion object {
        private const val ARG_ACTIONS = "actions"

        fun newInstance(actions: List<String>) = BottomMoreDialog().apply {
            arguments = Bundle().apply {
                putStringArray(ARG_ACTIONS, actions.toTypedArray())
            }
        }
    }
}
