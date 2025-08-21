package sky.kr.co.newtogetusa.ui.main.search

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomAreaSelect
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding, SearchViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_search
    override val viewModel: SearchViewModel by viewModels()

    override fun init() {
        super.init()
        dataBinding.clArea.isSelected = true
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer.collectLatest {
                    Timber.d("isModePlayer $it")
                    if(it){
                        findNavController().navigate(R.id.action_searchFragment_to_deliveryRequestSearchFragment)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isAreaSelectState.collectLatest { isArea ->
                    dataBinding.clArea.isSelected = isArea
                    dataBinding.clNear.isSelected = !isArea
                }
            }
        }

        viewModel.search.observe(viewLifecycleOwner){

        }

        viewModel.event.observe(viewLifecycleOwner){event ->
            when(event){
                SearchViewModel.Event.StartRegion -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelect()
                    )
                }
                SearchViewModel.Event.DestinationRegion -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAreaSelect()
                    )
                }
                SearchViewModel.Event.Search -> {
                    findNavController().navigate(R.id.action_searchFragment_to_searchResultFragment)
                    //findNavController().navigate(R.id.playerTermFragment)
                }
            }
        }
    }
}