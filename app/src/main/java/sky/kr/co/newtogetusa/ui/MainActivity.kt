package sky.kr.co.newtogetusa.ui

import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.databinding.ActivityMainBinding
import sky.kr.co.newtogetusa.ui.base.BaseActivity
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(){
    override val layoutId: Int
        get() = R.layout.activity_main
    override val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var appBarConfiguration: AppBarConfiguration

    //@Inject
    //lateinit var chatClient: ChatClient

    private var backKeyPressedTime: Long = 0
    private val finishDelayTime = 2000

    private val mainTabFragments = setOf(
        R.id.homeTabFragment, R.id.searchFragment, R.id.historyFragment, R.id.chattingTabFragment, R.id.myFragment, R.id.deliveryRequestSearchFragment,
        R.id.historyDeliveryFragment
    )

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.currentDestination?.id in mainTabFragments) {
                if (System.currentTimeMillis() - backKeyPressedTime > finishDelayTime) {
                    backKeyPressedTime = System.currentTimeMillis()
                    toast("앱을 끄려면 한 번 더 눌러주세요.")
                } else {
                    finishAffinity()
                }
            } else {
                //기기 전원버튼off/on시 처리추가
                navController.popBackStack()
            }
        }
    }

    override fun init() {
        super.init()
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment
        navController = navHostFragment.navController
        dataBinding.bottomNavigation.setupWithNavController(navController)
        dataBinding.bottomNavigation.setOnApplyWindowInsetsListener(null)
        dataBinding.bottomNavigation.itemIconTintList = null
        //remove tooltip
        try {
            dataBinding.bottomNavigation.menu.forEach {
                findViewById<View>(it.itemId).setOnLongClickListener { true }
            }
        } catch (e: Exception) {
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home, R.id.search, R.id.history, R.id.chat, R.id.my)
        )

        //chatClient.connect()
    }

    override fun initObserver() {
        super.initObserver()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Timber.d("onDestion  ${destination.label} , ${destination.route}")
            dataBinding.bottomNavigation.isVisible = destination.id in mainTabFragments
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //chatClient.disconnect()
    }

    private fun sendChatMessage(message: String){
        //chatClient.sendMessage(message)
    }

    fun showChangeModeAnimation(isShow:Boolean){
        viewModel.onModeChange(isShow)
    }

}