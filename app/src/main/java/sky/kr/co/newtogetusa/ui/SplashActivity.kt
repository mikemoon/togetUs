package sky.kr.co.newtogetusa.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ActivitySplashBinding
import sky.kr.co.newtogetusa.ui.base.BaseActivity
import sky.kr.co.newtogetusa.ui.login.LoginActivity

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashVM>() {
    override val layoutId: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        keepSplashFullscreen()
        super.onCreate(savedInstanceState)
        keepSplashFullscreen()
    }

    override fun init() {
        super.init()
        NaverIdLoginSDK.initialize(this, "dc6y2eYKSjjh77N8TUsu", "ewIZbsx8F_", "투겟어스")

        lifecycleScope.launch {
            delay(2000)
            when (viewModel.resolveStartDestination()) {
                SplashVM.StartDestination.FirstRunLogin -> {
                    startActivityWithoutTransition(
                        Intent(this@SplashActivity, LoginActivity::class.java).putExtra(
                            LoginActivity.EXTRA_START_PERMISSION,
                            true,
                        )
                    )
                }
                SplashVM.StartDestination.Login -> {
                    startActivityWithoutTransition(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                SplashVM.StartDestination.Main -> {
                    showModeUserAnimation()
                    delay(1500)
                    startActivityWithoutTransition(Intent(this@SplashActivity, MainActivity::class.java))
                }
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) keepSplashFullscreen()
    }

    private fun keepSplashFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun showModeUserAnimation() {
        dataBinding.imageLogo.isVisible = false
        dataBinding.animationModeUser.isVisible = true
        dataBinding.animationModeUser.playAnimation()
    }

    private fun startActivityWithoutTransition(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
