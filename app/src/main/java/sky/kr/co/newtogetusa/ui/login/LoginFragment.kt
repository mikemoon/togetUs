package sky.kr.co.newtogetusa.ui.login

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sky.kr.co.newtogetusa.BuildConfig
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login
    override val viewModel: LoginViewModel by activityViewModels()

    override fun init(){

    }

    override fun initObserver() {
        super.initObserver()

        Timber.d(Utility.getKeyHash(requireContext()))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.uiErrorMsg.filter { it.isNotEmpty() }.collectLatest { msg ->
                        requireContext().toast(msg)
                    }
                }
            }
        }

        viewModel.event.observe(this){
            when(it){
                is LoginViewModel.Event.KakaoLogin -> {
                    val kakaoUserApiClient = UserApiClient.instance
                    if(kakaoUserApiClient.isKakaoTalkLoginAvailable(requireContext())){
                        kakaoUserApiClient.loginWithKakaoTalk(requireContext()){ token, error ->
                            if(error != null){
                                Timber.d("kakaoTalkLogin Error : ${error}")
                                MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "${error.message}", rightBtn = "확인").show(childFragmentManager, "")
                            }else if(token != null){
                                Timber.d("kakaoTalkLogin Success : ${token.accessToken}")
                                handleKakaoToken(token.accessToken)
                            }
                        }
                    }else{
                        kakaoUserApiClient.loginWithKakaoAccount(requireContext()){ token, error ->
                            if(error != null){
                                Timber.d("kakaoAccountLogin Error : ${error}")
                                MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "${error.message}", rightBtn = "확인").show(childFragmentManager, "")
                            }else if(token != null){
                                Timber.d("kakaoAccountLogin Success : ${token.accessToken}")
                                handleKakaoToken(token.accessToken)
                            }
                        }
                    }
                }
                is LoginViewModel.Event.NaverLogin -> {
                    NaverIdLoginSDK.showDevelopersLog(true)
                    NaverIdLoginSDK.logout()
                    NaverIdLoginSDK.isRequiredCustomTabsReAuth = true
                    NaverIdLoginSDK.authenticate(requireContext(), object : OAuthLoginCallback {
                        override fun onError(errorCode: Int, message: String) {
                            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                            /*Toast.makeText(requireContext(),"errorCode:$errorCode, errorDesc:$errorDescription",
                                Toast.LENGTH_SHORT).show()*/
                            MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "${errorDescription}:$errorCode", rightBtn = "확인").show(childFragmentManager, "")
                        }

                        override fun onFailure(httpStatus: Int, message: String) {
                            Timber.e("errorCode:$httpStatus, errorDesc:$message")
                            MessageDialog.newInstance(msgTitle = "로그인 실패", msg = "${message}:$httpStatus", rightBtn = "확인").show(childFragmentManager, "")
                        }

                        override fun onSuccess() {
                            val accessToken = NaverIdLoginSDK.getAccessToken()
                            val refreshToken = NaverIdLoginSDK.getRefreshToken()
                            val expiresAt = NaverIdLoginSDK.getExpiresAt().toString()
                            val tokenType = NaverIdLoginSDK.getTokenType()
                            val state = NaverIdLoginSDK.getState().toString()
                            Timber.d("naverLoginSuccess token $accessToken")
                            //requireContext().toast("naver login success token :$accessToken")
                            viewModel.loginNaver(accessToken!!){ resultCode, errorData ->
                                when(resultCode){
                                    200 ->{
                                        requireContext().startActivity(Intent(requireActivity(), MainActivity::class.java))
                                        requireActivity().finish()
                                    }
                                    404 ->{
                                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment(
                                            userId = errorData?.user_id?:0,
                                            verifyCode = errorData?.verify_code?:""
                                        ))
                                    }
                                }
                            }
                        }

                    })
                }
                is LoginViewModel.Event.GoogleLogin -> {
                    Timber.d("=== Google Login Debug ===")
                    //launchGoogleLogin(requireContext())
                    // Google Play Services 상태 확인
                    val googleApiAvailability = GoogleApiAvailability.getInstance()
                    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())

                    if (resultCode != ConnectionResult.SUCCESS) {
                        Timber.e("Google Play Services unavailable: $resultCode")
                        if (googleApiAvailability.isUserResolvableError(resultCode)) {
                            googleApiAvailability.getErrorDialog(this, resultCode, 9000)?.show()
                        }
                        return@observe
                    }

                    val credentialManager = CredentialManager.create(requireContext())
                    val webClientId = "714408641644-k2ovfqon2a2psm9en15mfnidjspse88n.apps.googleusercontent.com"
                        //"714408641644-k2ovfqon2a2psm9en15mfnidjspse88n.apps.googleusercontent.com"
                    Timber.d("Using Web Client ID: $webClientId")

                    // 관대한 설정으로 시작
                    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)  // 모든 계정 허용
                        .setServerClientId(webClientId)        // 웹 클라이언트 ID 사용
                        .setAutoSelectEnabled(false)           // 수동 선택
                        .setNonce(null)
                        .build()

                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    lifecycleScope.launch {
                        try {
                            Timber.d("Starting credential request...")
                            val result = credentialManager.getCredential(
                                request = request,
                                context = requireContext(),
                            )
                            Timber.d("Login successful!")
                            handleSignIn(result)
                        } catch (e: GetCredentialCancellationException) {
                            Timber.d("User cancelled: ${e.message}")
                            // 사용자가 실제로 취소한 경우
                        } catch (e: GetCredentialException) {
                            Timber.e("Login failed: ${e.type} - ${e.localizedMessage}")
                            showDetailedError(e)
                        }
                    }
                }
                is LoginViewModel.Event.AppleLogin -> {
                    launchAppleLogin()
                }
                is LoginViewModel.Event.EmailLogin -> {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginEmailFragment())
                }
            }
        }
    }

    fun handleKakaoToken(token:String){
        viewModel.loginKakao(token){ resultCode, errorData ->
            when(resultCode){
                200 ->{
                    requireContext().startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                404 ->{
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment(
                        userId = errorData?.user_id?:0,
                        verifyCode = errorData?.verify_code?:""
                    ))
                }
            }
        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        Timber.d("handleSignIn $result")
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {

            // Passkey credential
            //is PublicKeyCredential -> {
            // Share responseJson such as a GetCredentialResponse on your server to
            // validate and authenticate
            //responseJson = credential.authenticationResponseJson
            //}

            // Password credential
            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
            }

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        requestGoogleAccessToken(googleIdTokenCredential.id)
                    } catch (e: GoogleIdTokenParsingException) {
                        Timber.e(e, "Unable to parse Google credential")
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
            }
        }
    }

    private fun showDetailedError(exception: GetCredentialException) {
        val message = when {
            exception.localizedMessage?.contains("cancelled by the user") == true -> {
                """
            Google 로그인이 취소되었습니다.
            
            해결 방법:
            1. Google Cloud Console에서 Android 클라이언트 ID 생성 확인
            2. OAuth 동의 화면에 테스트 사용자 추가
            3. SHA-1 인증서 올바른 등록 확인
            """.trimIndent()
            }
            exception.localizedMessage?.contains("Cannot find a matching credential") == true -> {
                "Google 계정을 기기에 추가하거나 Google Play 서비스를 업데이트해주세요."
            }
            else -> "Google 로그인 오류: ${exception.localizedMessage}"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Google 로그인 문제")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun requestGoogleAccessToken(accountName: String) {
        lifecycleScope.launch {
            val accessToken = runCatching {
                withContext(Dispatchers.IO) {
                    GoogleAuthUtil.getToken(
                        requireContext().applicationContext,
                        Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
                        "oauth2:openid email profile"
                    )
                }
            }.getOrElse { error ->
                Timber.e(error, "Unable to get Google OAuth access token")
                requireContext().toast("구글 인증 정보를 가져오지 못했습니다. 다시 시도해주세요.")
                return@launch
            }

            viewModel.loginGoogle(accessToken) { resultCode, errorData ->
                when (resultCode) {
                    200 -> {
                        requireContext().startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                    404 -> {
                        findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment(
                                userId = errorData?.user_id ?: 0,
                                verifyCode = errorData?.verify_code ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    private fun buildGoogleLoginUrl(): String {
        val clientId = "714408641644-vl4r3v545qifrc7i82e2mdq9aah93jb5.apps.googleusercontent.com"
        val redirectUri = "http://togetus.p-e.kr/auths/oauth2/authorize/google"
        val scope = "email%20profile"
        val responseType = "code"

        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=$clientId" +
                "&redirect_uri=$redirectUri" +
                "&response_type=$responseType" +
                "&scope=$scope"
    }

    private fun launchAppleLogin() {
        val clientId = BuildConfig.APPLE_SERVICE_ID
        val redirectUri = BuildConfig.APPLE_REDIRECT_URI
        if (clientId.isBlank() || redirectUri.isBlank()) {
            Timber.e("Apple login is not configured. serviceId=$clientId redirectUri=$redirectUri")
            return
        }

        val authUri = Uri.Builder()
            .scheme("https")
            .authority("appleid.apple.com")
            .path("auth/authorize")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code id_token")
            .appendQueryParameter("response_mode", "form_post")
            .appendQueryParameter("scope", "name email")
            .appendQueryParameter("state", UUID.randomUUID().toString())
            .build()

        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(requireContext(), authUri)
    }

}
