package sky.kr.co.newtogetusa.ui.dialog.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogPlayerApplyCompletedBinding
import sky.kr.co.newtogetusa.utils.dpToPx

class PlayerApplyCompletedDialog : DialogFragment() {

    private var _binding: DialogPlayerApplyCompletedBinding? = null
    private val binding: DialogPlayerApplyCompletedBinding
        get() = requireNotNull(_binding)

    override fun getTheme(): Int = R.style.RoundedCornersDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPlayerApplyCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvConfirm.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(314.dpToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
