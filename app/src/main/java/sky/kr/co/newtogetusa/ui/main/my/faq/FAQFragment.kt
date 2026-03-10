package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQCateDto
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQDto
import sky.kr.co.newtogetusa.databinding.FragmentFaqBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.custom.CustomItemDecoration
import sky.kr.co.newtogetusa.utils.HorizontalItemSpacingDecoration
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class FAQFragment :BaseFragment<FragmentFaqBinding, FAQViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_faq
    override val viewModel: FAQViewModel by viewModels()

    private var faqAdapter = FAQAdapter(::onCategorySelected)
    private lateinit var faqListAdapter: FAQListAdapter
    private var allFaqList: List<FAQDto> = emptyList()

    override fun init() {
        super.init()

        dataBinding.rvCategory.apply {
            adapter = faqAdapter
            addItemDecoration(HorizontalItemSpacingDecoration(8.dpToPx()))
        }

        faqListAdapter = FAQListAdapter(viewModel)
        dataBinding.rvFaq.apply {
            adapter = faqListAdapter
            addItemDecoration(CustomItemDecoration(context, ContextCompat.getDrawable(context, R.drawable.list_divider)))
        }

        viewModel.getFaqCateList()
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.etSearch.doAfterTextChanged {

        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.faqCateList.collect { categories ->
                        if (categories.isEmpty()) return@collect

                        val categoryItems = buildList {
                            add(FAQCateDto(cateId = 0, cateName = "전체"))
                            addAll(categories)
                        }
                        faqAdapter.setItems(categoryItems)

                        val fetchedFaqMap = categories.associate { category ->
                            category.cateId to async { viewModel.getFaqListByCategory(category.cateId) }
                        }.mapValues { it.value.await() }

                        allFaqList = categories.flatMap { fetchedFaqMap[it.cateId].orEmpty() }
                        faqListAdapter.setItems(allFaqList)
                    }
                }

                launch {
                    viewModel.faqList.collect {
                        faqListAdapter.setItems(it)
                    }
                }
            }
        }


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
                is FAQViewModel.Event.AskHistory -> {
                    findNavController().navigate(R.id.action_FAQFragment_to_askHistoryFragment)
                }
            }
        }
    }

    private fun onCategorySelected(category: FAQCateDto) {
        if (category.cateId == 0) {
            faqListAdapter.setItems(allFaqList)
            return
        }
        viewModel.getFaqList(category.cateId)
    }
}