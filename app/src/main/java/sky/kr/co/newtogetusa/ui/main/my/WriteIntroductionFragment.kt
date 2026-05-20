package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentWriteIntroductionBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class WriteIntroductionFragment : BaseFragment<FragmentWriteIntroductionBinding, WriteIntroductionViewModel>() {
    override val layoutId: Int = R.layout.fragment_write_introduction
    override val viewModel: WriteIntroductionViewModel by viewModels()
    private val args: WriteIntroductionFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        viewModel.setInitial(args.introduction.orEmpty())
        dataBinding.etIntroduction.setText(args.introduction.orEmpty())
        dataBinding.btnSave.setOnClickListener { viewModel.save(args.playerId) }
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.countText.collect { dataBinding.tvCount.text = it }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                WriteIntroductionViewModel.Event.Back -> findNavController().popBackStack()
                WriteIntroductionViewModel.Event.SaveSuccess -> {
                    requireContext().toast("자기소개가 저장되었습니다.")
                    findNavController().popBackStack()
                }
                WriteIntroductionViewModel.Event.SaveFailed -> requireContext().toast("자기소개 저장에 실패했습니다.")
            }
        }
    }
}
