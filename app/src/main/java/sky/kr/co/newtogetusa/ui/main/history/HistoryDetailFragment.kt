package sky.kr.co.newtogetusa.ui.main.history

import androidx.fragment.app.viewModels
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHistoryDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

class HistoryDetailFragment : BaseFragment<FragmentHistoryDetailBinding, HistoryDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_history_detail
    override val viewModel: HistoryDetailViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()
    }
}