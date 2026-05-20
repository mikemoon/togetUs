package sky.kr.co.newtogetusa.ui.main.global

import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentNoPictureBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class NoPictureFragment : BaseFragment<FragmentNoPictureBinding, NoPictureVM>() {

    override val layoutId: Int
        get() = R.layout.fragment_no_picture

    override val viewModel: NoPictureVM by viewModels()
    private val args: NoPictureFragmentArgs by navArgs()

    override fun init() {
        bindClicks()
        bindTextWatcher()
    }

    override fun initObserver() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                NoPictureVM.Event.CompleteSuccess -> {
                    Toast.makeText(requireContext(), "완료 처리되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }

                NoPictureVM.Event.CompleteFailed -> {
                    Toast.makeText(requireContext(), "완료 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

                NoPictureVM.Event.InvalidDeliveryId -> {
                    Toast.makeText(requireContext(), "배송 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindClicks() {
        dataBinding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        dataBinding.reasonOption1.setOnClickListener { viewModel.selectReason(0) }
        dataBinding.reasonOption2.setOnClickListener { viewModel.selectReason(1) }
        dataBinding.reasonOption3.setOnClickListener { viewModel.selectReason(2) }
        dataBinding.reasonOption4.setOnClickListener { viewModel.selectReason(3) }
        dataBinding.reasonOption5.setOnClickListener { viewModel.selectReason(4) }

        dataBinding.btnComplete.setOnClickListener {
            viewModel.requestCompleteWithoutPicture(args.deliveryId, args.proofType)
        }
    }

    private fun bindTextWatcher() {
        dataBinding.etReasonDetail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val currentText = s?.toString().orEmpty()
                if (currentText.length > NoPictureVM.MAX_REASON_LENGTH) {
                    val trimmed = currentText.take(NoPictureVM.MAX_REASON_LENGTH)
                    dataBinding.etReasonDetail.setText(trimmed)
                    dataBinding.etReasonDetail.setSelection(trimmed.length)
                    viewModel.updateReasonDetail(trimmed)
                } else {
                    viewModel.updateReasonDetail(currentText)
                }
            }
        })
    }

}
