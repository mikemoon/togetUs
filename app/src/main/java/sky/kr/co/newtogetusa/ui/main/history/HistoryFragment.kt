package sky.kr.co.newtogetusa.ui.main.history

import android.os.Message
import android.os.Parcelable
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.dialog.message.ReceiveConfirmDialog
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding, HistoryViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_history
    override val viewModel: HistoryViewModel by viewModels()
    private var savedState: Parcelable? = null

    private lateinit var historyAdapter: HistoryAdapter
    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.tvTopAll.isSelected = true
        historyAdapter = HistoryAdapter(viewModel)
        dataBinding.rvHistory.apply {
            adapter = historyAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
        //savedState = dataBinding.rvHistory.layoutManager?.onSaveInstanceState()
        viewLifecycleOwner.lifecycleScope.launch {
            historyAdapter.submitData(PagingData.from(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")))
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer.collectLatest {
                    Timber.d("isModePlayer $it")
                    if(it){
                        findNavController().navigate(R.id.action_historyFragment_to_historyDeliveryFragment)
                    }
                }
            }
        }

        viewModel.topMenuLiveData.observe(viewLifecycleOwner){ topMenu ->
            setSelectedTopMenu(topMenu)
        }

        viewModel.itemCancelLiveData.observe(viewLifecycleOwner){
            dialogFragmentShow(
                childFragmentManager,
                ReceiveConfirmDialog()
                /*MessageDialog.newInstance(
                    msg = "배송요청을 취소하시겠어요?",
                    leftBtn = "아니요",
                    rightBtn = "예"
                ).onRightBtn {

                }*/
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.historyFlow.collectLatest{
                    val adapter = (dataBinding.rvHistory.adapter as HistoryAdapter)
                    adapter.addLoadStateListener { loadState ->
                        if (loadState.refresh is LoadState.NotLoading) {
                            if (savedState != null) {
                                dataBinding.rvHistory.layoutManager?.onRestoreInstanceState(savedState)
                            } else {
                                savedState = dataBinding.rvHistory.layoutManager?.onSaveInstanceState()
                            }
                        }
                    }
                    //adapter.submitData(it)
                }
            }
        }
    }

    private fun setSelectedTopMenu(topMenu: HistoryViewModel.TopMenu){
        dataBinding.tvTopAll.isSelected = topMenu == HistoryViewModel.TopMenu.All
        dataBinding.tvTopDoing.isSelected = topMenu == HistoryViewModel.TopMenu.Doing
        dataBinding.tvTopEnd.isSelected = topMenu == HistoryViewModel.TopMenu.End
    }
}