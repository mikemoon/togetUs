package sky.kr.co.newtogetusa.ui.login

import android.app.AlertDialog
import android.widget.Toast
import androidx.activity.viewModels
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.ActivityLoginBinding
import sky.kr.co.newtogetusa.ui.base.BaseActivity
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModels()

    override fun init() {
        super.init()


        NaverIdLoginSDK.initialize(this, "dc6y2eYKSjjh77N8TUsu", "ewIZbsx8F_", "투겟어스")
        //startActivity(Intent(this, MainActivity::class.java))
        setupNavigation()
    }

    override fun initObserver() {
        super.initObserver()

    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fcv) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.login)
    }


}