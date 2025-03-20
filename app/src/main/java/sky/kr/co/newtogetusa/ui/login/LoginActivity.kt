package sky.kr.co.newtogetusa.ui.login

import android.content.Intent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ActivityLoginBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseActivity

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModels()

    override fun init() {
        super.init()

        startActivity(Intent(this, MainActivity::class.java))
    }
}