package sky.kr.co.newtogetusa.ui.main.history

import android.content.Context
import android.os.Parcelable
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.databinding.FragmentHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.ReceiveConfirmDialog
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRequestSharedViewModel
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding, HistoryViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_history
    override val viewModel: HistoryViewModel by viewModels()
    private var savedState: Parcelable? = null

    private val deliverySharedViewModel: DeliveryRequestSharedViewModel
            by hiltNavGraphViewModels(R.id.nav_graph)

    private lateinit var historyAdapter: HistoryAdapter
    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.root.post { clearSearchFocus() }
        setSelectedTopMenu(viewModel.selectedTopMenu.value)
        historyAdapter = HistoryAdapter(viewModel) { selectedItem ->
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.historyFragment) {
                navController.navigate(
                    R.id.action_global_historyDetailFragment,
                    bundleOf("delivery" to selectedItem)
                )
            }
        }
        dataBinding.rvHistory.apply {
            adapter = historyAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
    }

    override fun initObserver() {
        super.initObserver()

        historyAdapter.addLoadStateListener { loadState ->
            val isEmpty = loadState.refresh is LoadState.NotLoading && historyAdapter.itemCount == 0
            dataBinding.llEmpty.isVisible = isEmpty
            setSearchEnabled(!isEmpty)
            if (isEmpty && dataBinding.etSearch.text.isNullOrBlank()) {
                clearSearchFocus()
            }
        }

        dataBinding.ivSearch.setOnClickListener {
            if (!dataBinding.etSearch.isEnabled) return@setOnClickListener
            viewModel.setKeyword(dataBinding.etSearch.text.toString())
        }

        // 동행 푸시 수신 시 목록 갱신 (iOS refreshDeliveryList 대응)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRefreshBus.listEvents.collect {
                    historyAdapter.refresh()
                }
            }
        }

        dataBinding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                if (!dataBinding.etSearch.isEnabled) return@setOnEditorActionListener false
                viewModel.setKeyword(dataBinding.etSearch.text.toString())
                clearSearchFocus()
                true
            } else {
                false
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collectLatest {
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedTopMenu.collectLatest(::setSelectedTopMenu)
            }
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
            )
        }

        viewModel.menuButtonLiveData.observe(viewLifecycleOwner){ menuAction ->
            when(menuAction){
                is HistoryViewModel.MenuButton.MenuModify -> {
                    lifecycleScope.launch {
                        showLoading()
                        val item = menuAction.item
                        when (val res = viewModel.fetchDeliveryDetail(item.delivery_id)) {
                            is ResultWrapper.Success -> {
                                viewModel.deliveryDetail.value = res.data
                                populateDeliverySharedState()
                                findNavController().navigate(
                                    R.id.action_global_to_home_for_delivery,
                                    bundleOf(
                                        "openDeliveryReq" to true,
                                        "returnToHistory" to true,
                                        "isEdit" to true,
                                        "deliveryId" to item.delivery_id
                                    )
                                )
                            }
                            else -> {
                                requireContext().toast("동행요청 정보를 불러올 수 없습니다.")
                            }
                        }
                        hideLoading()
                    }
                }
                is HistoryViewModel.MenuButton.MenuDeliveryStatus -> {
                    findNavController().navigate(
                        HistoryFragmentDirections.actionHistoryFragmentToDeliveryStatusFragment(
                            menuAction.item.delivery_id
                        )
                    )
                }
                is HistoryViewModel.MenuButton.MenuChat -> {
                    findNavController().navigate(
                        HistoryFragmentDirections.actionHistoryFragmentToChatInProgressFragment(
                            menuAction.item.delivery_id
                        )
                    )
                }
                is HistoryViewModel.MenuButton.MenuReview -> {
                    findNavController().navigate(
                        HistoryFragmentDirections.actionHistoryFragmentToDeliveryReviewFragment(
                            menuAction.item.delivery_id,
                            false
                        )
                    )
                }
                is HistoryViewModel.MenuButton.MenuRefresh -> {
                    requireContext().toast(menuAction.message)
                    historyAdapter.refresh()
                }
                is HistoryViewModel.MenuButton.MenuError -> {
                    requireContext().toast(menuAction.message)
                }
            }
        }
    }

    private fun setSelectedTopMenu(topMenu: HistoryViewModel.TopMenu){
        dataBinding.tvTopAll.isSelected = topMenu == HistoryViewModel.TopMenu.All
        dataBinding.tvTopDoing.isSelected = topMenu == HistoryViewModel.TopMenu.Doing
        dataBinding.tvTopEnd.isSelected = topMenu == HistoryViewModel.TopMenu.End
    }

    private fun clearSearchFocus() {
        dataBinding.etSearch.clearFocus()
        dataBinding.root.requestFocus()
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(dataBinding.etSearch.windowToken, 0)
    }

    private fun setSearchEnabled(enabled: Boolean) {
        dataBinding.etSearch.apply {
            isEnabled = enabled
            isFocusable = enabled
            isFocusableInTouchMode = enabled
            isCursorVisible = enabled
            if (!enabled) clearFocus()
        }
        dataBinding.ivSearch.isEnabled = enabled
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
            volume = detail.product.volume_cd,
            typeLabel = viewModel.productTypeLabel(detail.product.type_cd),
            weightLabel = viewModel.productWeightLabel(detail.product.weight_cd),
            volumeLabel = viewModel.productVolumeLabel(detail.product.volume_cd)
        )
        deliverySharedViewModel.updateProductImages(detail.product.pictures)

        deliverySharedViewModel.updateUser(
            name = detail.depart_contact.name.orEmpty(),
            phone = detail.depart_contact.phone.orEmpty()
        )

        deliverySharedViewModel.setInternational(!detail.is_domestic)
    }
}
