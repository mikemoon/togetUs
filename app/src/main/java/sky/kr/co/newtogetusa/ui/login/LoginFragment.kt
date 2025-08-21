package sky.kr.co.newtogetusa.ui.login

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login
    override val viewModel: LoginViewModel by activityViewModels()

    override fun initObserver() {
        super.initObserver()

        Timber.d(Utility.getKeyHash(requireContext()))

        viewModel.event.observe(this){
            when(it){
                is LoginViewModel.Event.KakaoLogin -> {
                    //requireContext().startActivity(Intent(requireActivity(), MainActivity::class.java))
                    //return@observe
                    val kakaoUserApiClient = UserApiClient.instance
                    if(kakaoUserApiClient.isKakaoTalkLoginAvailable(requireContext())){
                        kakaoUserApiClient.loginWithKakaoTalk(requireContext()){ token, error ->
                            if(error != null){
                                Timber.d("kakaoTalkLogin Error : ${error}")
                            }else if(token != null){
                                Timber.d("kakaoTalkLogin Success : ${token.accessToken}")
                                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment())
                            }
                        }
                    }else{
                        kakaoUserApiClient.loginWithKakaoAccount(requireContext()){ token, error ->
                            if(error != null){
                                Timber.d("kakaoAccountLogin Error : ${error}")
                            }else if(token != null){
                                Timber.d("kakaoAccountLogin Success : ${token.accessToken}")
                                viewModel.loginKakao(token.accessToken)
                                //findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment())
                            }
                        }
                    }
                }
                is LoginViewModel.Event.NaverLogin -> {
                    NaverIdLoginSDK.showDevelopersLog(true)
                    NaverIdLoginSDK.authenticate(requireContext(), object : OAuthLoginCallback {
                        override fun onError(errorCode: Int, message: String) {
                            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                            Toast.makeText(requireContext(),"errorCode:$errorCode, errorDesc:$errorDescription",
                                Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(httpStatus: Int, message: String) {
                            Timber.e("errorCode:$httpStatus, errorDesc:$message")
                        }

                        override fun onSuccess() {
                            val accessToken = NaverIdLoginSDK.getAccessToken()
                            val refreshToken = NaverIdLoginSDK.getRefreshToken()
                            val expiresAt = NaverIdLoginSDK.getExpiresAt().toString()
                            val tokenType = NaverIdLoginSDK.getTokenType()
                            val state = NaverIdLoginSDK.getState().toString()
                            Timber.d("naverLoginSuccess token $accessToken")
                            requireContext().toast("naver login success token :$accessToken")
                            viewModel.loginNaver(accessToken!!)
                        //findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment())
                        }

                    })
                }
                is LoginViewModel.Event.GoogleLogin -> {
                    Timber.d("=== Google Login Debug ===")
                    launchGoogleLogin(requireContext())
                    // Google Play Services 상태 확인
                    /*val googleApiAvailability = GoogleApiAvailability.getInstance()
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
                    }*/
                }
                is LoginViewModel.Event.EmailLogin -> {
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginEmailFragment())
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
                        Timber.d("data.type : ${googleIdTokenCredential.id}")
                        Timber.d("data.type : ${googleIdTokenCredential.displayName}")
                        Timber.d("data.type : ${googleIdTokenCredential.profilePictureUri.toString()}")

                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginTermAgreeFragment())
                    } catch (e: GoogleIdTokenParsingException) {

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


    fun launchGoogleLogin(context: Context) {
        val url = "http://togetus.p-e.kr/auths/oauth2/authorize/google"//buildGoogleLoginUrl()
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

}