package sky.kr.co.newtogetusa.ui.main.search

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentSearchResultBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomFilterDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class SearchResultFragment  : BaseFragment<FragmentSearchResultBinding, SearchResultViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_search_result
    override val viewModel: SearchResultViewModel by viewModels()

    var departList : List<String>? = null
    var destList : List<String>? = null

    override fun init() {
        super.init()
        dataBinding.llEmpty.isVisible = false
        dataBinding.rv.apply {
            adapter = SearchResultAdapter(listOf("홍길동", "김길동", "박길동"))
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                SearchResultViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                SearchResultViewModel.Event.Filter ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomFilterDialog().apply {
                            itemSelectCallback = { selectedItem ->
                                this@SearchResultFragment.dataBinding.tvFilter.text = selectedItem
                            }
                        }
                    )
                }
            }
        }
    }
}