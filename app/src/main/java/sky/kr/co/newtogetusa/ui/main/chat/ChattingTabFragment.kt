package sky.kr.co.newtogetusa.ui.main.chat

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentChattingBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ChattingTabFragment : BaseFragment<FragmentChattingBinding, ChattingTabViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_chatting
    override val viewModel: ChattingTabViewModel by viewModels()



    override fun init() {
        super.init()

        with(dataBinding.vPager){
            adapter = ChatViewPagerAdapter(this@ChattingTabFragment)
        }

        TabLayoutMediator(dataBinding.tab, dataBinding.vPager){ tab, position ->
            tab.text = when(position){
                0 -> {
                    getString(R.string.player)
                }
                1 ->{
                    getString(R.string.user)
                }
                else -> {
                    ""
                }
            }
        }.attach()

        for(i in 0 until dataBinding.tab.tabCount){
            dataBinding.tab.getTabAt(i)?.view?.setOnLongClickListener { true }
        }
    }


}