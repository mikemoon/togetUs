package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomSuggestDeliveryBinding

@AndroidEntryPoint
class BottomSuggestDeliveryDialog : BottomBaseDialog<DialogBottomSuggestDeliveryBinding, BottomSuggestDeliveryViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_suggest_delivery
    override val viewModel: BottomSuggestDeliveryViewModel by viewModels()

    var nickname: String = ""
    var requestItems: List<SuggestRequestItem> = listOf(
        SuggestRequestItem(
            title = "노트북 좀 전달해주세요",
            routeText = "서울 강서구 - 서울 강서구"
        ),
        SuggestRequestItem(
            title = "의류 좀 전달해주세요",
            routeText = "서울 강서구 - 일본 도쿄"
        )
    )
    var selectedRequestCallback: ((SuggestRequestItem) -> Unit)? = null

    private val requestAdapter = BottomSuggestDeliveryAdapter()

    override fun init() {
        super.init()

        dataBinding.tvDescription.text = "{배달왕}$nickname" + "님에게 배송을 제안하시겠어요?"

        dataBinding.rvRequests.adapter = requestAdapter
        requestAdapter.submitList(requestItems)
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                BottomSuggestDeliveryViewModel.Event.Close -> dismissAllowingStateLoss()
                BottomSuggestDeliveryViewModel.Event.Confirm -> {
                    requestAdapter.getSelectedItem()?.let { selectedItem ->
                        selectedRequestCallback?.invoke(selectedItem)
                    }
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    data class SuggestRequestItem(
        val title: String,
        val routeText: String
    )
}