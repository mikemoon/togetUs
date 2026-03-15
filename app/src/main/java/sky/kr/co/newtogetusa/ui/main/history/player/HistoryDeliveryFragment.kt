package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHistoryDeliveryBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber

@AndroidEntryPoint
class HistoryDeliveryFragment : BaseFragment<FragmentHistoryDeliveryBinding, HistoryDeliveryViewModel>() {
    override val layoutId: Int = R.layout.fragment_history_delivery
    override val viewModel: HistoryDeliveryViewModel by viewModels()

    private lateinit var historyAdapter: HistoryDeliverAdapter

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.tvTopAll.isSelected = true
        historyAdapter = HistoryDeliverAdapter(viewModel)

        dataBinding.rvHistory.apply {
            adapter = historyAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
        //savedState = dataBinding.rvHistory.layoutManager?.onSaveInstanceState()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isModePlayer
                        .filterNotNull()
                        .distinctUntilChanged()
                        .collectLatest {
                        Timber.d("isModePlayer $it")
                        val navController = findNavController()
                        if (
                            !it &&
                            navController.currentDestination?.id == R.id.historyDeliveryFragment
                        ) {
                            navController.navigate(R.id.action_historyDeliveryFragment_to_historyFragment)
                        }
                    }
                }

                launch {
                    viewModel.deliveryPagingFlow.collectLatest { pagingData ->
                        historyAdapter.submitData(pagingData)
                    }
                }
            }
        }
    }
}