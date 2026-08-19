package sky.kr.co.newtogetusa.ui.main.my.likeorder

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.NavGraphDirections
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLikeOrderBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRefreshBus
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class LikeOrderFragment : BaseFragment<FragmentLikeOrderBinding, LikeOrderViewModel>() {

    override val layoutId: Int = R.layout.fragment_like_order
    override val viewModel: LikeOrderViewModel by viewModels()

    private val likeOrderAdapter = LikeOrderAdapter { item ->
        findNavController().navigate(NavGraphDirections.actionGlobalHistoryDetailFragment(item))
    }

    override fun init() {
        super.init()
        dataBinding.rvOrders.apply {
            adapter = likeOrderAdapter
            itemAnimator = null
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
        dataBinding.swipeRefresh.setOnRefreshListener {
            likeOrderAdapter.refresh()
        }
        bindStatusClicks()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                LikeOrderViewModel.Event.Back -> findNavController().popBackStack()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deliveryPagingData.collectLatest { pagingData ->
                        likeOrderAdapter.submitData(pagingData)
                    }
                }
                // 동행 푸시 수신 시 목록 갱신 (iOS refreshDeliveryList 대응)
                launch {
                    DeliveryRefreshBus.listEvents.collect {
                        likeOrderAdapter.refresh()
                    }
                }
                launch {
                    viewModel.selectedStatus.collectLatest {
                        updateStatusUi(it)
                    }
                }
                launch {
                    likeOrderAdapter.loadStateFlow.collectLatest { loadState ->
                        val isRefreshing = loadState.refresh is LoadState.Loading
                        dataBinding.swipeRefresh.isRefreshing = isRefreshing
                        dataBinding.emptyView.isVisible =
                            loadState.refresh is LoadState.NotLoading && likeOrderAdapter.itemCount == 0
                    }
                }
            }
        }
    }

    private fun bindStatusClicks() {
        dataBinding.tvAll.setOnClickListener { viewModel.selectStatus(LikeOrderViewModel.Status.ALL) }
        dataBinding.tvProgress.setOnClickListener { viewModel.selectStatus(LikeOrderViewModel.Status.PROGRESS) }
        dataBinding.tvComplete.setOnClickListener { viewModel.selectStatus(LikeOrderViewModel.Status.COMPLETE) }
    }

    private fun updateStatusUi(status: LikeOrderViewModel.Status) {
        setChipSelected(dataBinding.tvAll, status == LikeOrderViewModel.Status.ALL)
        setChipSelected(dataBinding.tvProgress, status == LikeOrderViewModel.Status.PROGRESS)
        setChipSelected(dataBinding.tvComplete, status == LikeOrderViewModel.Status.COMPLETE)
    }

    private fun setChipSelected(view: android.widget.TextView, selected: Boolean) {
        val context = view.context
        view.isSelected = selected
        view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, if (selected) R.color.black_80 else R.color.white)
        )
        view.setTextColor(ContextCompat.getColor(context, if (selected) R.color.white else R.color.black_40))
    }
}
