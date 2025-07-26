package sky.kr.co.newtogetusa.ui.main.my.favor

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentFavorPlayerBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.HorizontalItemSpacingDecoration
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class FavorPlayerFragment : BaseFragment<FragmentFavorPlayerBinding, FavorPlayerViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_favor_player
    override val viewModel: FavorPlayerViewModel by viewModels()

    private lateinit var categoryAdapter: FavorPlayerCategoryAdapter
    private lateinit var contentAdapter: FavorPlayerListAdapter

    override fun init() {
        super.init()

        categoryAdapter = FavorPlayerCategoryAdapter(viewModel).apply {
            setItems(listOf("전체", "거래 진행", "거래 종료"))
        }
        dataBinding.rvCategory.apply {
            adapter = categoryAdapter
            itemAnimator = null
            addItemDecoration(HorizontalItemSpacingDecoration(8.dpToPx()))
        }

        contentAdapter = FavorPlayerListAdapter(viewModel).apply {
            setItems(listOf(1,2,3))
        }
        dataBinding.rv.apply {
            adapter = contentAdapter
            itemAnimator = null
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }

    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                FavorPlayerViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
            }
        }
    }
}