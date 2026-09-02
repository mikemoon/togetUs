package sky.kr.co.newtogetusa.ui.main.my

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import io.portone.sdk.android.PortOne
import io.portone.sdk.android.identityverification.IdentityVerificationCallback
import io.portone.sdk.android.type.request.IdentityVerificationRequest
import io.portone.sdk.android.type.response.IdentityVerificationResponse
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.BuildConfig
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentMyBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.dialog.message.PlayerApplyCompletedDialog
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding, MyViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_my
    override val viewModel: MyViewModel by viewModels()

    private val identityVerificationActivityResultLauncher =
        PortOne.registerForIdentityVerificationActivity(
            this,
            callback = object : IdentityVerificationCallback {
                override fun onSuccess(response: IdentityVerificationResponse) {
                    Timber.d(
                        "Identity verification success id=${response.identityVerificationId}, txId=${response.identityVerificationTxId}"
                    )
                    viewModel.verifyIdentity(response.identityVerificationId) {
                        navigatePlayerJoin()
                    }
                }

                override fun onFail(response: IdentityVerificationResponse) {
                    Timber.d(
                        "Identity verification failed code=${response.code}, message=${response.message}, pgCode=${response.pgCode}, pgMessage=${response.pgMessage}, id=${response.identityVerificationId}, txId=${response.identityVerificationTxId}"
                    )
                    showIdentityVerificationFailed(response.message)
                }
            }
        )

    override fun init() {
        super.init()

        dataBinding.tvVersion.text = "V${BuildConfig.VERSION_NAME}"
        viewModel.refreshProfileForMode {
            dataBinding.profile = it
        }
        viewModel.getPlayerInfo()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfileForMode {
            dataBinding.profile = it
        }
        viewModel.getPlayerInfo()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileDto.filterNotNull().collectLatest { profile ->
                        dataBinding.profile = profile
                    }
                }
                launch {
                    viewModel.isModeChanging.drop(1).filter { it }.collectLatest {
                        (requireActivity() as MainActivity).showChangeModeAnimation(!viewModel.isPlayerModeFlow.value)
                    }
                }
                launch {
                    viewModel.isPlayerModeFlow.collectLatest {
                        viewModel.refreshProfileForMode { profile ->
                            dataBinding.profile = profile
                        }
                    }
                }
                launch {
                    viewModel.isCompanyInfoExpanded.collectLatest { expanded ->
                        dataBinding.tvCompanyDetail.isVisible = expanded
                        dataBinding.ivCompanyArrow.rotation = if (expanded) 180f else 0f
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) {
            when (it) {
                MyViewModel.Event.MySetting -> {
                    val action = MyFragmentDirections.actionMyFragmentToMySettingFragment(viewModel.profileDto.value)
                    findNavController().navigate(action)
                }

                MyViewModel.Event.ProfileManage -> {
                    findNavController().navigate(
                        MyFragmentDirections.actionMyFragmentToProfileManagementFragment(
                            profileDto = viewModel.profileDto.value,
                            isPlayer = viewModel.isPlayerModeFlow.value
                        )
                    )
                }

                MyViewModel.Event.Notice -> {
                    findNavController().navigate(R.id.action_myFragment_to_noticeFragment)
                }

                MyViewModel.Event.FAQ -> {
                    findNavController().navigate(R.id.action_myFragment_to_FAQFragment)
                }

                MyViewModel.Event.Settle -> {
                    findNavController().navigate(R.id.action_myFragment_to_settleFragment)
                }

                MyViewModel.Event.LikePlayer -> {
                    findNavController().navigate(R.id.action_myFragment_to_likePlayerFragment)
                }

                MyViewModel.Event.Favor -> {
                    findNavController().navigate(R.id.action_myFragment_to_likeOrderFragment)
                }

                MyViewModel.Event.JoinPlayer -> {
                    val dto = viewModel.playerApplyedInfoDto.value
                    if (!dto?.certi_req_date.isNullOrEmpty() && dto.certi_res_date.isNullOrEmpty()) {
                        PlayerApplyCompletedDialog().show(childFragmentManager, "PlayerApplyCompletedDialog")
                        return@observe
                    }

                    if (viewModel.hasPlayerApplyRequest.value) {
                        MessageDialog.newInstance(
                            msg = "신청 중인 내역이 있어요. 이어서 진행하시겠어요?",
                            rightBtn = "예",
                            leftBtn = "아니오"
                        ).onRightBtn {
                            findNavController().navigate(
                                R.id.action_myFragment_to_playerJoinFragment2,
                                bundleOf("isResume" to true)
                            )
                        }
                            .onLeftBtn {

                            }
                            .show(childFragmentManager, "")
                    } else {
                        val profileId = viewModel.profileDto.value?.user?.player_id
                        if(profileId == null || profileId == 0) {
                            viewModel.startIdentityVerification()
                        }else{
                            navigatePlayerJoin()
                        }
                    }
                }

                MyViewModel.Event.AccompanyCredit -> {
                    viewModel.openAccompanyCreditNotice()
                }

                MyViewModel.Event.AccompanyCreditFallback -> {
                    findNavController().navigate(R.id.action_myFragment_to_accompanyCreditFragment)
                }

                is MyViewModel.Event.NoticeDetail -> {
                    findNavController().navigate(
                        R.id.noticeDetailFragment,
                        bundleOf("id" to it.id)
                    )
                }

                MyViewModel.Event.Update -> {
                    openPlayStore()
                }

                MyViewModel.Event.Term -> {
                    findNavController().navigate(MyFragmentDirections.actionMyFragmentToTermFragment())
                }

                MyViewModel.Event.UseHistory -> {
                    findNavController().navigate(
                        R.id.action_global_historyFragment,
                        bundleOf(
                            "fromMySubMenu" to true
                        )
                    )
                }

                is MyViewModel.Event.StartIdentityVerification -> {
                    startPortOneIdentityVerification(it.config.storeId, it.config.channelKey)
                }

                is MyViewModel.Event.ShowMessage -> {
                    requireContext().toast(it.message)
                }
            }
        }
    }

    private fun startPortOneIdentityVerification(storeId: String, channelKey: String) {
        val identityVerificationId = UUID.randomUUID().toString().replace("-", "")
        Timber.d(
            "Identity verification request storeId=$storeId, channelKey=$channelKey, identityVerificationId=$identityVerificationId"
        )

        PortOne.requestIdentityVerification(
            requireActivity(),
            request = IdentityVerificationRequest(
                storeId = storeId,
                identityVerificationId = identityVerificationId,
                channelKey = channelKey
            ),
            resultLauncher = identityVerificationActivityResultLauncher
        )
    }

    private fun showIdentityVerificationFailed(message: String?) {
        MessageDialog.newInstance(
            msg = message.toIdentityFailureMessage(),
            rightBtn = "확인",
            msgTitle = "본인인증 실패"
        ).show(childFragmentManager, "")
    }

    private fun String?.toIdentityFailureMessage(): String {
        val message = this?.trim().orEmpty()
        return when {
            message.isBlank() -> "본인인증에 실패했습니다."
            message.contains("알수 없는 이유") -> message.replace("알수 없는 이유", "알 수 없는 이유")
            else -> message
        }
    }

    private fun navigatePlayerJoin() {
        findNavController().navigate(
            R.id.action_myFragment_to_playerJoinFragment2,
            bundleOf("isResume" to false)
        )
    }

    private fun openPlayStore() {
        val packageName = requireContext().packageName
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
            setPackage("com.android.vending")
        }

        try {
            startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

}
