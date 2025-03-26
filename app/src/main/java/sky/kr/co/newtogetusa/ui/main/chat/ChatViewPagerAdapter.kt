package sky.kr.co.newtogetusa.ui.main.chat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatViewPagerAdapter(fm: Fragment):FragmentStateAdapter(fm) {

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ChattingPlayerFragment()
            1 -> ChattingUserFragment()
            else -> ChattingPlayerFragment()
        }
    }

    override fun getItemCount(): Int = 2
}