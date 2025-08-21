package sky.kr.co.newtogetusa.ui.main.search.player

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryRequestSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomDeliveryFilterDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomFilterDialog
import sky.kr.co.newtogetusa.utils.VerticalSpaceItemDecoration
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber

@AndroidEntryPoint
class DeliveryRequestSearchFragment : BaseFragment<FragmentDeliveryRequestSearchBinding, DeliveryRequestSearchViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_request_search
    override val viewModel: DeliveryRequestSearchViewModel by viewModels()

    private lateinit var deliveryRequestSearchAdapter: DeliveryRequestSearchAdapter

    override fun init() {
        super.init()

        deliveryRequestSearchAdapter = DeliveryRequestSearchAdapter().apply {
            setItems(listOf(1,2,3))
        }
        dataBinding.rv.apply {
            adapter = deliveryRequestSearchAdapter
            addItemDecoration(VerticalSpaceItemDecoration(20.dpToPx()))
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer.collectLatest {
                    Timber.d("isModePlayer $it")
                    if(!it){
                        findNavController().navigate(R.id.action_deliveryRequestSearchFragment_to_searchFragment)
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                DeliveryRequestSearchViewModel.Event.Sort -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomFilterDialog().apply {
                            filterList = listOf("가까운 거리순", "최신 등록순")
                        }
                    )
                }
                DeliveryRequestSearchViewModel.Event.Filter -> {
                    Timber.d("filter")
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomDeliveryFilterDialog().apply {
                        }
                    )
                }
            }
        }

    }
}