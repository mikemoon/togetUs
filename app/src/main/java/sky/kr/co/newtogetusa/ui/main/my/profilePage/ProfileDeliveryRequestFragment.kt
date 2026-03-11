package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentProfileDeliveryRequestBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.my.ProfileManagementViewModel
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class ProfileDeliveryRequestFragment : BaseFragment<FragmentProfileDeliveryRequestBinding, ProfileManagementViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_delivery_request
    override val viewModel: ProfileManagementViewModel by viewModels({requireParentFragment()})

    private lateinit var deliveryReqAdapter: DeliveryReqAdapter

    override fun init() {
        super.init()

        deliveryReqAdapter = DeliveryReqAdapter(viewModel)
        deliveryReqAdapter.addOnPagesUpdatedListener {
            focusTopPosition()
        }
        dataBinding.rv.apply {
            adapter = deliveryReqAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
            setPadding(paddingLeft, 20.dpToPx(), paddingRight, 20.dpToPx())
            clipToPadding = false
        }
        viewModel.onTopMenuSelect(viewModel.menuAll)
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deliveryPagingFlow.collectLatest { pagingData ->
                        deliveryReqAdapter.submitData(pagingData)
                    }
                }
            }
        }

        viewModel.topMenuLiveData.observe(viewLifecycleOwner) {
            deliveryReqAdapter.notifyItemChanged(0)
            focusTopPosition()
        }
    }

    private fun focusTopPosition() {
        dataBinding.rv.post {
            dataBinding.rv.scrollToPosition(0)
        }
    }
}