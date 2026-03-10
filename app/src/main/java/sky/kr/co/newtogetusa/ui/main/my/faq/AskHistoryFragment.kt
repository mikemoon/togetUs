package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
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
        historyAdapter.setItems(listOf("1","2","3"))
        dataBinding.rv.apply {
            adapter = historyAdapter
            addItemDecoration(CustomItemDecoration(context, ContextCompat.getDrawable(context, R.drawable.list_divider)))
        }
        viewModel.getOneOnOne()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                AskHistoryViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                is AskHistoryViewModel.Event.AskItem -> {
                    findNavController().navigate(R.id.action_askHistoryFragment_to_askDetailFragment)
                }
            }
        }
    }
}