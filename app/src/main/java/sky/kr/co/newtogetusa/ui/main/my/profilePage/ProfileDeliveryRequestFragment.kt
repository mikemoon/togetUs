package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
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

        deliveryReqAdapter = DeliveryReqAdapter()
        deliveryReqAdapter.setItems(listOf("a", "b", "c"))
        dataBinding.rv.apply {
            adapter = deliveryReqAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
            setPadding(paddingLeft, 20.dpToPx(), paddingRight, 20.dpToPx())
            clipToPadding = false
        }
    }

    override fun initObserver() {
        super.initObserver()
    }
}