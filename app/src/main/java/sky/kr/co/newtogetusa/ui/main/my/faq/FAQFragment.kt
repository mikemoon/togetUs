package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentFaqBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.custom.CustomItemDecoration

@AndroidEntryPoint
class FAQFragment :BaseFragment<FragmentFaqBinding, FAQViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_faq
    override val viewModel: FAQViewModel by viewModels()

    private var faqAdapter = FAQAdapter()
    private lateinit var faqListAdapter: FAQListAdapter

    override fun init() {
        super.init()

        faqAdapter.setItems(listOf("전체", "공통", "플레이어", "유저"))
        dataBinding.rvCategory.apply {
            adapter = faqAdapter
        }

        faqListAdapter = FAQListAdapter(viewModel)
        faqListAdapter.setItems(listOf(
            "투겟어스 자주 묻는 질문입니다 제목은 최대 두줄까지 표시되며 초과되면 말줄임 표시를 합니...",
            "수령한 물품에 문제가 생겼어요",
            "정산 주기는 어떻게 되나요?"
        ))
        dataBinding.rvFaq.apply {
            adapter = faqListAdapter
            addItemDecoration(CustomItemDecoration(context, ContextCompat.getDrawable(context, R.drawable.list_divider)))
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is FAQViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is FAQViewModel.Event.FAQDetail -> {
                    findNavController().navigate(R.id.action_FAQFragment_to_FAQDetailFragment)
                }
                is FAQViewModel.Event.Ask -> {
                    findNavController().navigate(R.id.action_FAQFragment_to_askFragment)
                }
            }
        }
    }
}