package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAskHistoryBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.custom.CustomItemDecoration

@AndroidEntryPoint
class AskHistoryFragment :BaseFragment<FragmentAskHistoryBinding, AskHistoryViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_ask_history
    override val viewModel: AskHistoryViewModel by viewModels()

    private lateinit var historyAdapter: AskHistoryAdapter

    override fun init() {
        super.init()
        historyAdapter = AskHistoryAdapter(viewModel)
        dataBinding.rv.apply {
            adapter = historyAdapter
            addItemDecoration(CustomItemDecoration(context, ContextCompat.getDrawable(context, R.drawable.list_divider)))
        }
        viewModel.getOneOnOne()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.inquiryList.collectLatest {
                    historyAdapter.setItems(it)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                AskHistoryViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is AskHistoryViewModel.Event.AskItem -> {
                    val action = AskHistoryFragmentDirections.actionAskHistoryFragmentToAskDetailFragment(it.data)
                    findNavController().navigate(action)
                }
            }
        }
    }
}