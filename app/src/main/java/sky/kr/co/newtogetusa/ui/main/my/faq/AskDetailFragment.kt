package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAskDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.loadImage

@AndroidEntryPoint
class AskDetailFragment : BaseFragment<FragmentAskDetailBinding, AskDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_ask_detail
    override val viewModel: AskDetailViewModel by viewModels()

    val args : AskDetailFragmentArgs by navArgs()

    override fun init() {
        super.init()

        val data = args.data
        dataBinding.tvState.apply {
            text = when (data.status) {
                "WAIT" -> "답변대기"
                else -> "답변완료"
            }
            setTextColor(requireContext().getColor(if (data.status == "WAIT")R.color.primary_100 else R.color.black_60))
            background = requireContext().getDrawable(if (data.status == "WAIT") R.drawable.background_s_primary5_r4 else R.drawable.background_s_b5_r4)
        }
        dataBinding.tvTitle.text = data.title
        dataBinding.tvDate.text = data.inquiryDate
        dataBinding.tvContent.text = data.inquiry
        runCatching {
            dataBinding.iv.loadImage(data.inquiryAttachs[0])
        }.onFailure {  }

        data.response?.let {
            dataBinding.tvResContent.text = it
            dataBinding.tvResReady.isVisible = false
        }
        if(data.responseDate.isNotEmpty()){
            dataBinding.tvResDate.text = data.responseDate
        }
        if(data.responseAttachs.isNotEmpty()){
            runCatching {
                dataBinding.ivRes.loadImage(data.responseAttachs[0])
            }.onFailure {  }
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                AskDetailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}