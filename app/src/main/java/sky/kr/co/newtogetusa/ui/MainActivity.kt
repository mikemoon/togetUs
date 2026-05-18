package sky.kr.co.newtogetusa.ui

import android.content.Intent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.auth.AuthSessionManager
import sky.kr.co.newtogetusa.chat.ChatClient
import sky.kr.co.newtogetusa.databinding.ActivityMainBinding
import sky.kr.co.newtogetusa.ui.base.BaseActivity
import sky.kr.co.newtogetusa.ui.login.LoginActivity
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import javax.inject.Inject
import kotlin.jvm.java

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
    @Inject
    lateinit var authSessionManager: AuthSessionManager

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
        setFirebaseToken()
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

        viewModel.connect()
    }

    override fun initObserver() {
        super.initObserver()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Timber.d("onDestion  ${destination.label} , ${destination.route}")
            dataBinding.bottomNavigation.isVisible = destination.id in mainTabFragments
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                authSessionManager.sessionExpiredFlow.collect {
                    viewModel.clearSessionData()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.isPlayerModeFlow.collectLatest { isPlayerMode ->
                    dataBinding.bottomNavigation.menu.findItem(R.id.history)?.title =
                        if (isPlayerMode) "동행내역" else "이용내역"
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.hasUnreadChatFlow.collectLatest { hasUnread ->
                    updateChatTabBadge(hasUnread)
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    private fun setFirebaseToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            viewModel.postFCMToken(token)
        })
    }

    private fun sendChatMessage(message: String){
        //chatClient.sendMessage(message)
    }

    fun showChangeModeAnimation(isShow:Boolean){
        viewModel.onModeChange(isShow)
    }

    fun selectMainTab(itemId: Int) {
        dataBinding.bottomNavigation.selectedItemId = itemId
    }

    private fun updateChatTabBadge(hasUnread: Boolean) {
        val badge = dataBinding.bottomNavigation.getOrCreateBadge(R.id.chat).apply {
            clearNumber()
            backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.red_100)
            badgeGravity = BadgeDrawable.TOP_END
            isVisible = hasUnread
        }
        if (!hasUnread) {
            dataBinding.bottomNavigation.removeBadge(R.id.chat)
        }
    }

}
