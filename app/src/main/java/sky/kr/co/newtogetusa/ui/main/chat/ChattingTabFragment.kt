package sky.kr.co.newtogetusa.ui.main.chat

import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
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
            isUserInputEnabled = false
        }

        TabLayoutMediator(dataBinding.tab, dataBinding.vPager, false, false){ tab, position ->
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
        setupTabViews()

        for(i in 0 until dataBinding.tab.tabCount){
            dataBinding.tab.getTabAt(i)?.view?.setOnLongClickListener { true }
        }

    }

    private fun setupTabViews() {
        setTabView(position = 0, title = getString(R.string.player), badge = "100+")
        setTabView(position = 1, title = getString(R.string.user), badge = "2")
        updateTabView(dataBinding.tab.selectedTabPosition)
        dataBinding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabView(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setTabView(position: Int, title: String, badge: String) {
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.item_chat_top_tab, null)
        customView.findViewById<TextView>(R.id.tvTabTitle).text = title
        customView.findViewById<TextView>(R.id.tvTabBadge).text = badge
        dataBinding.tab.getTabAt(position)?.customView = customView
    }

    private fun updateTabView(selectedPosition: Int) {
        for (index in 0 until dataBinding.tab.tabCount) {
            val tabView = dataBinding.tab.getTabAt(index)?.customView ?: continue
            val title = tabView.findViewById<TextView>(R.id.tvTabTitle)
            val badge = tabView.findViewById<TextView>(R.id.tvTabBadge)
            val selected = index == selectedPosition
            title.setTextColor(requireContext().getColor(if (selected) R.color.primary_100 else R.color.black_40))
            badge.setTextColor(requireContext().getColor(if (selected) R.color.primary_100 else R.color.black_40))
            badge.setBackgroundResource(if (selected) R.drawable.background_s_primary5_r4 else R.drawable.background_s_b5_r4)
        }
    }

}
