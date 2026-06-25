package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.portone.sdk.android.PortOne
import io.portone.sdk.android.identityverification.IdentityVerificationCallback
import io.portone.sdk.android.type.request.IdentityVerificationRequest
import io.portone.sdk.android.type.response.IdentityVerificationResponse
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentProfileMangeSubBinding
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
class ProfileManageSubFragment : BaseFragment<FragmentProfileMangeSubBinding, ProfileManageSubVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_profile_mange_sub
    override val viewModel: ProfileManageSubVM by viewModels()

    private val args : ProfileManageSubFragmentArgs by navArgs()

    private val identityVerificationActivityResultLauncher =
        PortOne.registerForIdentityVerificationActivity(
            fragment = this,
            callback = object : IdentityVerificationCallback {
                override fun onSuccess(response: IdentityVerificationResponse) {
                    Timber.d(
                        "Profile recertification success id=${response.identityVerificationId}, txId=${response.identityVerificationTxId}"
                    )
                    viewModel.verifyIdentity(response.identityVerificationId)
                }

                override fun onFail(response: IdentityVerificationResponse) {
                    Timber.e(
                        "Profile recertification failed code=${response.code}, message=${response.message}, pgCode=${response.pgCode}, pgMessage=${response.pgMessage}, id=${response.identityVerificationId}, txId=${response.identityVerificationTxId}"
                    )
                    requireContext().toast(response.message ?: "본인인증에 실패했습니다.")
                }
            }
        )

    override fun init() {
        super.init()

        dataBinding.profile = args.profileDto
        dataBinding.tvPhone.text = "-"
        dataBinding.tvDate.text = "※ 최근 인증: -"
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loginAccountText.collectLatest {
                        dataBinding.tvEmail.text = it
                    }
                }
                launch {
                    viewModel.loginTypeLabel.collectLatest { type ->
                        dataBinding.tvSnsTitle.text = if (type == "이메일") "이메일 로그인" else "SNS 로그인"
                        val iconRes = when (type) {
                            "카카오" -> R.drawable.login_kakao
                            "네이버" -> R.drawable.login_naver
                            "구글" -> R.drawable.login_google
                            "이메일" -> R.drawable.login_mail
                            else -> R.drawable.login_mail
                        }
                        dataBinding.ivSns.setImageResource(iconRes)
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ProfileManageSubVM.Event.Back -> {
                    findNavController().popBackStack()
                }
                is ProfileManageSubVM.Event.ModifyProfile -> {
                    findNavController().navigate(ProfileManageSubFragmentDirections.actionProfileManageSubFragmentToModifyProfileFragment(args.profileDto))
                }
                is ProfileManageSubVM.Event.PasswordSet -> {
                    findNavController().navigate(R.id.action_profileManageSubFragment_to_passwordSetFragment)
                }
                ProfileManageSubVM.Event.Recertification -> Unit
                is ProfileManageSubVM.Event.StartIdentityVerification -> {
                    startPortOneIdentityVerification(it.config.storeId, it.config.channelKey)
                }
                ProfileManageSubVM.Event.IdentityVerified -> {
                    requireContext().toast("본인인증이 완료되었습니다.")
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "재인증 완료",
                            msg = "휴대폰 본인인증이 완료되었습니다.",
                            rightBtn = "확인"
                        )
                    )
                }
                is ProfileManageSubVM.Event.ShowMessage -> {
                    requireContext().toast(it.message)
                }
            }
        }
    }

    private fun startPortOneIdentityVerification(storeId: String, channelKey: String) {
        val identityVerificationId = UUID.randomUUID().toString().replace("-", "")
        Timber.d(
            "Profile recertification request storeId=$storeId, channelKey=$channelKey, identityVerificationId=$identityVerificationId"
        )
        PortOne.requestIdentityVerification(
            activity = requireActivity(),
            request = IdentityVerificationRequest(
                storeId = storeId,
                identityVerificationId = identityVerificationId,
                channelKey = channelKey
            ),
            resultLauncher = identityVerificationActivityResultLauncher
        )
    }
}
