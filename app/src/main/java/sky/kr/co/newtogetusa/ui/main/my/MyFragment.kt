package sky.kr.co.newtogetusa.ui.main.my

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentMyBinding
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber

@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding, MyViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_my
    override val viewModel: MyViewModel by viewModels()

    override fun init() {
        super.init()

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

                MyViewModel.Event.Favor -> {
                    findNavController().navigate(R.id.action_myFragment_to_likeOrderFragment)
                }

                MyViewModel.Event.JoinPlayer -> {
                    val dto = viewModel.playerApplyedInfoDto.value
                    if (!dto?.certi_req_date.isNullOrEmpty() && dto.certi_res_date.isNullOrEmpty()) {
                        findNavController().navigate(R.id.action_myFragment_to_playerJoinCompleteFragment)
                        return@observe
                    }

                    if (viewModel.hasPlayerApplyRequest.value) { //플레이어 신청 있는 상태
                        MessageDialog.newInstance(
                            msg = "신청 중인 내역이 있어요. 이어서 진행하시겠어요?",
                            rightBtn = "예",
                            leftBtn = "아니오"
                        ).onRightBtn {
                            findNavController().navigate(R.id.action_myFragment_to_playerJoinFragment2)
                        }
                            .onLeftBtn {

                            }
                            .show(childFragmentManager, "")
                    } else {
                        val profileId = viewModel.profileDto.value?.user?.player_id
                        if(viewModel.impUidString.value.isEmpty() && (profileId == null || profileId == 0)) {
                            //본인인증 flow

                            //서버인증
                            viewModel.setTempImpUid()
                            viewModel.verifyImpUid(viewModel.impUidString.value){ playerId ->
                                findNavController().navigate(R.id.action_myFragment_to_playerJoinFragment2)
                            }
                        }else{
                            findNavController().navigate(R.id.action_myFragment_to_playerJoinFragment2)
                        }
                    }
                }

                MyViewModel.Event.AccompanyCredit -> {
                    findNavController().navigate(R.id.action_myFragment_to_accompanyCreditFragment)
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

                MyViewModel.Event.FavorPlayer -> {
                    findNavController().navigate(R.id.action_myFragment_to_favorPlayerFragment)
                }
            }
        }
    }

}
