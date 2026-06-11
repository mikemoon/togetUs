package sky.kr.co.newtogetusa.ui.login

import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ActivityLoginBinding
import sky.kr.co.newtogetusa.ui.base.BaseActivity

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModels()

    override fun init() {
        super.init()

        setupNavigation()
    }

    override fun initObserver() {
        super.initObserver()

    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fcv) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.login)
        if (intent.getBooleanExtra(EXTRA_START_PERMISSION, false)) {
            navGraph.setStartDestination(R.id.loginPermission)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }
        navController.graph = navGraph
    }

    companion object {
        const val EXTRA_START_PERMISSION = "extra_start_permission"
    }

}
