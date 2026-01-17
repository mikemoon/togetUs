package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import sky.kr.co.newtogetusa.ui.main.my.profilePage.ProfileDeliveryRequestFragment
import sky.kr.co.newtogetusa.ui.main.my.profilePage.ProfileIntroduceFragment
import sky.kr.co.newtogetusa.ui.main.my.profilePage.ProfileReviewFragment

class ProfileManagementPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    isPlayerMode : Boolean
) : FragmentStateAdapter(fm, lifecycle) {
    private val isPlayerMode = isPlayerMode
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment =
        when(position){
            0 -> if(isPlayerMode)ProfileIntroduceFragment() else ProfileDeliveryRequestFragment()
            else -> ProfileReviewFragment()
        }
}