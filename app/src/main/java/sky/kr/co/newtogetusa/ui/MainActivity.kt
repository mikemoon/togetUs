package sky.kr.co.newtogetusa.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
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
    private var hasCheckedNotificationSettings = false
    private var isNotificationSettingsDialogShowing = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted || !areAppNotificationsEnabled()) {
                showNotificationSettingsDialog()
            }
        }

    private val mainTabFragments = setOf(
        R.id.homeTabFragment, R.id.searchFragment, R.id.historyFragment, R.id.chattingTabFragment, R.id.myFragment, R.id.deliveryRequestSearchFragment,
        R.id.historyDeliveryFragment
    )

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isCurrentMainTabDestination()) {
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
        dataBinding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.home) {
                navigateToHomeTabRoot()
                true
            } else {
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
        dataBinding.bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.home) {
                navigateToHomeTabRoot()
            }
        }
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
        handlePushIntent(intent)
    }

    override fun initObserver() {
        super.initObserver()
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Timber.d("onDestion  ${destination.label} , ${destination.route}")
            dataBinding.bottomNavigation.isVisible = isMainTabDestination(destination.id, arguments)
            if (dataBinding.bottomNavigation.isVisible) {
                viewModel.refreshChatUnread()
            }
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
                viewModel.unreadChatCountFlow.collectLatest { unreadCount ->
                    updateChatTabBadge(unreadCount)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshChatUnread()
        checkAppNotificationSettingsIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
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
        navigateToHomeTabRoot()
    }

    fun selectMainTab(itemId: Int) {
        dataBinding.bottomNavigation.selectedItemId = itemId
    }

    private fun navigateToHomeTabRoot() {
        if (!::navController.isInitialized) return

        runCatching {
            val popped = navController.popBackStack(R.id.homeTabFragment, false)
            if (!popped && navController.currentDestination?.id != R.id.homeTabFragment) {
                navController.navigate(R.id.action_global_home)
            }
            if (dataBinding.bottomNavigation.selectedItemId != R.id.home) {
                dataBinding.bottomNavigation.menu.findItem(R.id.home).isChecked = true
            }
        }.onFailure {
            Timber.e(it, "Failed to navigate home after mode change")
        }
    }

    private fun isCurrentMainTabDestination(): Boolean {
        val destinationId = navController.currentDestination?.id ?: return false
        return isMainTabDestination(destinationId, navController.currentBackStackEntry?.arguments)
    }

    private fun isMainTabDestination(destinationId: Int, arguments: android.os.Bundle?): Boolean {
        if (destinationId !in mainTabFragments) return false
        if (destinationId == R.id.historyFragment && arguments?.getBoolean("fromMySubMenu") == true) {
            return false
        }
        return true
    }

    private fun updateChatTabBadge(unreadCount: Int) {
        if (unreadCount <= 0) {
            dataBinding.bottomNavigation.removeBadge(R.id.chat)
            return
        }

        val badge = dataBinding.bottomNavigation.getOrCreateBadge(R.id.chat).apply {
            number = unreadCount
            maxCharacterCount = 3
            backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.red_100)
            badgeTextColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            badgeGravity = BadgeDrawable.TOP_END
            isVisible = true
        }
    }

    private fun handlePushIntent(intent: Intent?) {
        if (!::navController.isInitialized) return
        val openHomeFromPush = intent?.getBooleanExtra(EXTRA_OPEN_HOME_FROM_PUSH, false) == true
        val pushType = intent?.getStringExtra(EXTRA_PUSH_TYPE).orEmpty()
        val roomId = intent?.getLongExtra(EXTRA_PUSH_ROOM_ID, -1L) ?: -1L
        val isLegacyChatPush = pushType == PUSH_TYPE_CHAT && roomId > 0L
        if (!openHomeFromPush && !isLegacyChatPush) return

        intent?.removeExtra(EXTRA_OPEN_HOME_FROM_PUSH)
        intent?.removeExtra(EXTRA_PUSH_TYPE)
        intent?.removeExtra(EXTRA_PUSH_ROOM_ID)

        runCatching {
            navigateToHomeTabRoot()
        }.onFailure {
            Timber.e(it, "Failed to navigate home from push")
        }
    }

    private fun checkAppNotificationSettingsIfNeeded() {
        if (hasCheckedNotificationSettings) return
        hasCheckedNotificationSettings = true

        if (areAppNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showNotificationSettingsDialog()
        }
    }

    private fun areAppNotificationsEnabled(): Boolean =
        NotificationManagerCompat.from(this).areNotificationsEnabled()

    private fun showNotificationSettingsDialog() {
        if (isFinishing || isDestroyed || isNotificationSettingsDialogShowing) return
        isNotificationSettingsDialogShowing = true

        MessageDialog.newInstance(
            msgTitle = "알림 설정",
            msg = "동행 요청, 채팅, 매칭 상태 알림을 받으려면 시스템 설정에서 앱 알림을 켜주세요.",
            leftBtn = "나중에",
            rightBtn = "설정으로 이동",
        ).onLeftBtn {
            isNotificationSettingsDialogShowing = false
        }.onRightBtn {
            isNotificationSettingsDialogShowing = false
            openAppNotificationSettings()
        }.show(supportFragmentManager, NOTIFICATION_SETTINGS_DIALOG_TAG)
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        runCatching {
            startActivity(intent)
        }.onFailure {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }

    companion object {
        const val EXTRA_OPEN_HOME_FROM_PUSH = "extra_open_home_from_push"
        const val EXTRA_PUSH_TYPE = "extra_push_type"
        const val EXTRA_PUSH_ROOM_ID = "extra_push_room_id"
        private const val PUSH_TYPE_CHAT = "chat"
        private const val NOTIFICATION_SETTINGS_DIALOG_TAG = "NotificationSettingsDialog"
    }

}
