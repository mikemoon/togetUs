package sky.kr.co.newtogetusa.ui.main.my

import android.app.AlertDialog
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentWithdrawBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.login.LoginActivity
import sky.kr.co.newtogetusa.ui.login.LoginViewModel
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import kotlin.jvm.java

@AndroidEntryPoint
class WithDrawFragment : BaseFragment<FragmentWithdrawBinding, WithDrawViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_withdraw
    override val viewModel: WithDrawViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginType.filterNotNull().collectLatest { loginType ->
                        when (loginType) {
                            LoginViewModel.GOOGLE -> {
                                dataBinding.llSns.isVisible = true
                                dataBinding.llSns.background = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.background_st_b20_s_w_r4
                                )
                                dataBinding.ivSns.setImageResource(R.drawable.login_google)
                                dataBinding.tvSns.text = "구글로 인증하기"
                            }

                            LoginViewModel.KAKAO -> {
                                dataBinding.llSns.isVisible = true
                            }

                            LoginViewModel.NAVER -> {
                                dataBinding.llSns.isVisible = true
                                dataBinding.llSns.background = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.background_s_03c75a_r4
                                )
                                dataBinding.ivSns.setImageResource(R.drawable.login_naver)
                                dataBinding.tvSns.apply {
                                    text = "네이버로 인증하기"
                                    setTextColor(ContextCompat.getColor(context, R.color.white))
                                }
                            }

                            LoginViewModel.EMAIL -> {
                                dataBinding.tvEmailVerify.isVisible = true
                            }
                        }
                    }
                }

                launch {
                    viewModel.isSnsVerified.collectLatest { isVerified ->
                        dataBinding.tvWithDraw.isEnabled = isVerified
                        if (isVerified) {
                            dataBinding.tvSns.isClickable = false
                        }
                    }
                }

                launch {
                    viewModel.isEmailVerified.collectLatest { isVerified ->
                        if (viewModel.loginType.value == LoginViewModel.EMAIL) {
                            dataBinding.tvWithDraw.isEnabled = isVerified
                        }
                    }
                }
            }
        }

        dataBinding.llSns.setOnClickListener {
            when (viewModel.loginType.value) {
                LoginViewModel.KAKAO -> verifyKakao()
                LoginViewModel.NAVER -> verifyNaver()
                LoginViewModel.GOOGLE -> verifyGoogle()
            }
        }

        dataBinding.tvNext.setOnClickListener {
            viewModel.verifyEmail(dataBinding.etPw.text.toString()) { isVerified, message ->
                if (isVerified) {
                    requireContext().toast("인증이 완료되었습니다.")
                    viewModel.withDrawStep.value = 1
                } else {
                    MessageDialog.newInstance(
                        msgTitle = "",
                        msg = message ?: "인증에 실패했습니다.",
                        rightBtn = "확인"
                    ).show(childFragmentManager, "")
                }
            }
        }

        dataBinding.etPw.doAfterTextChanged { text ->
            dataBinding.tvNext.isEnabled = (text?.length ?: 0) >= 4
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                WithDrawViewModel.Event.Back -> {
                    if (viewModel.withDrawStep.value == 1) {
                        findNavController().popBackStack()
                    } else {
                        viewModel.withDrawStep.value = 1
                    }
                }

                WithDrawViewModel.Event.WithDraw -> {
                    Timber.d("withDraw ${viewModel.isSnsVerified.value}, ${viewModel.isEmailVerified.value}")
                    if (viewModel.isSnsVerified.value || viewModel.isEmailVerified.value) {
                        showWithDrawConfirmDialog(false, false)
                    }
                }

                WithDrawViewModel.Event.VerifyEmail -> {
                    viewModel.withDrawStep.value = 2
                }
            }
        }
    }

    private fun verifyKakao() {
        val kakaoUserApiClient = UserApiClient.instance
        if (kakaoUserApiClient.isKakaoTalkLoginAvailable(requireContext())) {
            kakaoUserApiClient.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Timber.d("kakaoTalk verify error : $error")
                } else if (token != null) {
                    onSnsVerified()
                }
            }
        } else {
            kakaoUserApiClient.loginWithKakaoAccount(requireContext()) { token, error ->
                if (error != null) {
                    Timber.d("kakaoAccount verify error : $error")
                } else if (token != null) {
                    onSnsVerified()
                }
            }
        }
    }

    private fun verifyNaver() {
        NaverIdLoginSDK.authenticate(requireContext(), object : OAuthLoginCallback {
            override fun onError(errorCode: Int, message: String) {
                Timber.d("naver verify error : $errorCode / $message")
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Timber.d("naver verify failure : $httpStatus / $message")
            }

            override fun onSuccess() {
                onSnsVerified()
            }
        })
    }

    private fun verifyGoogle() {
        lifecycleScope.launch {
            val webClientId = "714408641644-k2ovfqon2a2psm9en15mfnidjspse88n.apps.googleusercontent.com"
            val credentialManager = CredentialManager.create(requireContext())
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                when (val credential = result.credential) {
                    is PasswordCredential -> Unit
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                GoogleIdTokenCredential.createFrom(credential.data)
                                onSnsVerified()
                            } catch (_: GoogleIdTokenParsingException) {
                            }
                        }
                    }
                    else -> Unit
                }
            } catch (_: GetCredentialCancellationException) {
            } catch (e: GetCredentialException) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Google 인증 문제")
                    .setMessage(e.localizedMessage ?: "Google 인증에 실패했습니다.")
                    .setPositiveButton("확인", null)
                    .show()
            }
        }
    }

    private fun showWithDrawConfirmDialog(hasDelivery: Boolean, hasDeliveryReq: Boolean){
        val title = if(hasDelivery)"진행중인 배송이 있어요" else if(hasDeliveryReq) "진행중인 배송요청이 있어요" else "정말 탈퇴하시겠어요?"
        val message = when{
            hasDelivery || hasDeliveryReq ->{"진행중인 배송이 있으면 탈퇴할 수 없습니다.\n" +
                    "취소 또는 완료 후 다시 신청해주세요."}
            else ->{"회원 탈퇴 시 계정 및 이용 기록이 모두 삭제되며 복구할 수 없습니다.  \n" +
                    "'탈퇴하기' 버튼을 누르시면 즉시 탈퇴가 완료됩니다."}
        }
        val rightBtnText = when{
            hasDelivery || hasDeliveryReq ->"확인"
            else ->"탈퇴하기"
        }
        val leftBtnText = when{
            hasDelivery || hasDeliveryReq -> ""
            else ->"취소"
        }
        MessageDialog.newInstance(
            msgTitle = title,
            msg = message,
            rightBtn = rightBtnText,
            leftBtn = leftBtnText
        ).onRightBtn {
            when{
                hasDelivery || hasDeliveryReq ->{

                }
                else ->{
                    this@WithDrawFragment.viewModel.withDraw{
                        if(it){
                            requireContext().toast("탈퇴가 완료되었습니다.\n" +
                                    "그동안 이용해 주셔서 감사합니다.")
                            lifecycleScope.launch {
                                delay(500)
                                startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                                requireActivity().finish()
                            }
                        }
                    }
                }
            }
        }.show(childFragmentManager, "")
    }

    private fun onSnsVerified() {
        viewModel.setSnsVerified(true)
        requireContext().toast("인증이 완료되었습니다.")
    }
}
