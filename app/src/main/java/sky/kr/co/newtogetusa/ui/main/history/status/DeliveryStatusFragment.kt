package sky.kr.co.newtogetusa.ui.main.history.status

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryStatusBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class DeliveryStatusFragment : BaseFragment<FragmentDeliveryStatusBinding, DeliveryStatusViewModel>() {

    override val layoutId: Int = R.layout.fragment_delivery_status
    override val viewModel: DeliveryStatusViewModel by viewModels()

    private val args: DeliveryStatusFragmentArgs by navArgs()
    private val statusAdapter = DeliveryStatusAdapter()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        dataBinding.rvStatus.adapter = statusAdapter
        viewModel.load(args.deliveryId)
    }

    override fun initObserver() {
        super.initObserver()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collect { items ->
                        statusAdapter.submitItems(items)
                        dataBinding.llEmpty.isVisible = items.isEmpty()
                    }
                }
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        dataBinding.progress.isVisible = isLoading
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryStatusViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryStatusViewModel.Event.LoadFailed -> requireContext().toast("동행 현황을 불러오지 못했습니다.")
            }
        }
    }
}
