package sky.kr.co.newtogetusa.ui.main.my.block

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentBlockManagementBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class BlockManagementFragment : BaseFragment<FragmentBlockManagementBinding, BlockManagementViewModel>() {
    override val layoutId: Int = R.layout.fragment_block_management
    override val viewModel: BlockManagementViewModel by viewModels()

    private val blockAdapter = BlockManagementAdapter { item ->
        viewModel.unblock(item)
    }

    override fun init() {
        super.init()
        dataBinding.rvBlocks.adapter = blockAdapter
        viewModel.loadBlockedItems()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                BlockManagementViewModel.Event.Back -> findNavController().popBackStack()
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            requireContext().toast(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { items ->
                    blockAdapter.setItems(items)
                    dataBinding.tvEmpty.isVisible = items.isEmpty()
                }
            }
        }
    }
}
