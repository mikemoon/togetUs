package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomSuggestDeliveryBinding

@AndroidEntryPoint
class BottomSuggestDeliveryDialog : BottomBaseDialog<DialogBottomSuggestDeliveryBinding, BottomSuggestDeliveryViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_suggest_delivery
    override val viewModel: BottomSuggestDeliveryViewModel by viewModels()

    var nickname: String = ""
    var requestItems: List<SuggestRequestItem> = emptyList()
    var selectedRequestCallback: ((SuggestRequestItem) -> Unit)? = null

    private val requestAdapter = BottomSuggestDeliveryAdapter { selectedItem ->
        selectedRequestCallback?.invoke(selectedItem)
        dismissAllowingStateLoss()
    }

    override fun init() {
        super.init()

        dataBinding.tvDescription.text = "$nickname 님에게 동행을 제안하시겠어요?"

        dataBinding.rvRequests.adapter = requestAdapter
        requestAdapter.submitList(requestItems)
        viewModel.loadRegisteredDeliveries()
    }

    override fun initObserver() {
        super.initObserver()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.requestItems.collectLatest {
                    requestAdapter.submitList(it)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                BottomSuggestDeliveryViewModel.Event.Close -> dismissAllowingStateLoss()
            }
        }
    }

    data class SuggestRequestItem(
        val deliveryId: Long,
        val title: String,
        val routeText: String
    )
}
