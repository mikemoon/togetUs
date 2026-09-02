package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ActivityLoginBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseActivity
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModels()
    private var navController: NavController? = null

    override fun init() {
        super.init()

        setupNavigation()
        handleAppleLoginRedirect(intent)
    }

    override fun initObserver() {
        super.initObserver()

    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fcv) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.login)
        if (intent.getBooleanExtra(EXTRA_START_PERMISSION, false)) {
            navGraph.setStartDestination(R.id.loginPermission)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }
        navHostFragment.navController.graph = navGraph
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAppleLoginRedirect(intent)
    }

    private fun handleAppleLoginRedirect(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != APPLE_LOGIN_SCHEME || uri.host != APPLE_LOGIN_HOST) return

        val error = uri.getQueryOrFragmentParameter("error")
        if (!error.isNullOrBlank()) {
            MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "Apple 로그인에 실패했습니다.", rightBtn = "확인")
                .show(supportFragmentManager, "")
            return
        }

        val appleToken = uri.getQueryOrFragmentParameter("id_token")
            ?: uri.getQueryOrFragmentParameter("identity_token")
            ?: uri.getQueryOrFragmentParameter("access_token")
            ?: uri.getQueryOrFragmentParameter("code")

        if (appleToken.isNullOrBlank()) {
            MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "Apple 인증 정보를 가져오지 못했습니다.", rightBtn = "확인")
                .show(supportFragmentManager, "")
            return
        }

        viewModel.loginApple(appleToken) { resultCode, errorData ->
            when (resultCode) {
                200 -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                404 -> {
                    navController?.navigate(
                        R.id.loginTermAgreeFragment,
                        bundleOf(
                            "userId" to (errorData?.user_id ?: 0),
                            "verify_code" to (errorData?.verify_code ?: "")
                        )
                    )
                }
                else -> {
                    MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "Apple 로그인에 실패했습니다.", rightBtn = "확인")
                        .show(supportFragmentManager, "")
                }
            }
        }
    }

    private fun Uri.getQueryOrFragmentParameter(key: String): String? {
        getQueryParameter(key)?.let { return it }
        val fragmentValue = fragment ?: return null
        return fragmentValue
            .split("&")
            .mapNotNull { part ->
                val index = part.indexOf("=")
                if (index <= 0) null else part.substring(0, index) to Uri.decode(part.substring(index + 1))
            }
            .firstOrNull { it.first == key }
            ?.second
    }

    companion object {
        const val EXTRA_START_PERMISSION = "extra_start_permission"
        private const val APPLE_LOGIN_SCHEME = "togetus-apple"
        private const val APPLE_LOGIN_HOST = "login"
    }

}
