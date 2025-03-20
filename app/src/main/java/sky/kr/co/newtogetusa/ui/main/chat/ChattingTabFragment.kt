package sky.kr.co.newtogetusa.ui.main.chat

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingTabFragment : BaseFragment<FragmentChattingBinding, ChattingTabViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_chatting
    override val viewModel: ChattingTabViewModel by viewModels()
}