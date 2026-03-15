package sky.kr.co.newtogetusa.ui.main.history

import android.os.Message
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.LoadState
import androidx.paging.PagingData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.databinding.FragmentHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.dialog.message.ReceiveConfirmDialog
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRequestSharedViewModel
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding, HistoryViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_history
    override val viewModel: HistoryViewModel by viewModels()
    private var savedState: Parcelable? = null

    private val deliverySharedViewModel: DeliveryRequestSharedViewModel
            by navGraphViewModels(R.id.nav_graph)

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

    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.ivSearch.setOnClickListener {
            viewModel.setKeyword(dataBinding.etSearch.text.toString())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer.collectLatest {
                    Timber.d("isModePlayer $it")
                    val navController = findNavController()
                    if (
                        it &&
                        navController.currentDestination?.id == R.id.historyFragment
                    ) {
                        navController.navigate(R.id.action_historyFragment_to_historyDeliveryFragment)
                    }
                }
            }
        }

        viewModel.topMenuLiveData.observe(viewLifecycleOwner){ topMenu ->
            setSelectedTopMenu(topMenu)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryPagingFlow.collectLatest { pagingData ->
                    historyAdapter.submitData(pagingData)
                }
            }
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

        viewModel.menuButtonLiveData.observe(viewLifecycleOwner){ menuAction ->
            when(menuAction){
                is HistoryViewModel.MenuButton.MenuModify -> {
                    findNavController().navigate(
                        R.id.action_global_to_home_for_delivery,
                        bundleOf("openDeliveryReq" to true)
                    )
                }
            }
        }

        /*viewLifecycleOwner.lifecycleScope.launch {
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
        }*/
    }

    private fun setSelectedTopMenu(topMenu: HistoryViewModel.TopMenu){
        dataBinding.tvTopAll.isSelected = topMenu == HistoryViewModel.TopMenu.All
        dataBinding.tvTopDoing.isSelected = topMenu == HistoryViewModel.TopMenu.Doing
        dataBinding.tvTopEnd.isSelected = topMenu == HistoryViewModel.TopMenu.End
    }

    private fun populateDeliverySharedState() {
        val detail = viewModel.deliveryDetail.value ?: return
        deliverySharedViewModel.clearState()

        deliverySharedViewModel.updateDistance(
            distance = detail.expected.expected_distance.toString()
        )

        deliverySharedViewModel.updateStartLocation(
            address = detail.depart.address,
            detail = detail.depart.address2.orEmpty(),
            lat = detail.depart.latitude,
            lng = detail.depart.longitude
        )

        deliverySharedViewModel.updateDestinationLocation(
            address = detail.dest.address,
            detail = detail.dest.address2.orEmpty(),
            lat = detail.dest.latitude,
            lng = detail.dest.longitude
        )

        deliverySharedViewModel.updatePickupInfo(
            isImmediately = detail.pickup.is_immediately,
            date = detail.pickup.date,
            time = detail.pickup.time.orEmpty(),
            isFaceToFace = detail.pickup.is_face2face
        )

        deliverySharedViewModel.updateProductInfo(
            title = detail.product.name,
            description = detail.product.descript,
            type = detail.product.type_cd,
            weight = detail.product.weight_cd,
            volume = detail.product.volume_cd
        )

        deliverySharedViewModel.updateUser(
            name = detail.depart_contact.name.orEmpty(),
            phone = detail.depart_contact.phone.orEmpty()
        )

        deliverySharedViewModel.setInternational(!detail.is_domestic)
    }
}