package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentPlayerAreaSettingBinding
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddedRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaAddRequest
import sky.kr.co.newtogetusa.data.remote.request.player.PlayerAreaLocationRequest
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class PlayerAreaSettingFragment : BaseFragment<FragmentPlayerAreaSettingBinding, ProfileManagementViewModel>() {
    override val layoutId: Int = R.layout.fragment_player_area_setting
    override val viewModel: ProfileManagementViewModel by viewModels()

    override fun init() {
        super.init()
        dataBinding.ivBack.setOnClickListener { findNavController().popBackStack() }
        dataBinding.btnAddBasic.setOnClickListener {
            val route = readRouteOrToast() ?: return@setOnClickListener
            viewModel.addPlayerArea(
                PlayerAreaAddRequest(
                    is_domestic = dataBinding.swDomestic.isChecked,
                    enable = true,
                    depart = route.first,
                    dest = route.second,
                )
            )
        }
        dataBinding.btnAddAdded.setOnClickListener {
            val route = readRouteOrToast() ?: return@setOnClickListener
            val start = dataBinding.etStartDate.text?.toString().orEmpty().trim()
            val end = dataBinding.etEndDate.text?.toString().orEmpty().trim()
            if (start.length != 8 || end.length != 8) {
                requireContext().toast("시작일과 종료일을 yyyyMMdd 형식으로 입력해 주세요.")
                return@setOnClickListener
            }
            viewModel.addPlayerAreaAdded(
                PlayerAreaAddedRequest(
                    start = start,
                    end = end,
                    enable = true,
                    depart = route.first,
                    dest = route.second,
                )
            )
        }
        viewModel.areaAddResult.observe(viewLifecycleOwner) { success ->
            requireContext().toast(if (success) "지역이 추가되었습니다." else "지역 추가에 실패했습니다.")
            if (success) reloadProfile()
        }
        reloadProfile()
    }

    private fun readRouteOrToast(): Pair<PlayerAreaLocationRequest, PlayerAreaLocationRequest>? {
        val depart = dataBinding.etDepart.text?.toString().orEmpty().trim()
        val dest = dataBinding.etDest.text?.toString().orEmpty().trim()
        if (depart.isBlank() || dest.isBlank()) {
            requireContext().toast("출발지와 도착지를 입력해 주세요.")
            return null
        }
        return PlayerAreaLocationRequest(address = depart) to PlayerAreaLocationRequest(address = dest)
    }

    private fun reloadProfile() {
        viewModel.getMyProfile {
            viewModel.getPlayerProfile { profile ->
                val basic = profile.areas_basic.orEmpty().joinToString("\n") {
                    "${it.depart_address.orEmpty()} > ${it.dest_address.orEmpty()}"
                }
                val added = profile.areas_added.orEmpty().joinToString("\n") {
                    "${it.depart_address.orEmpty()} > ${it.dest_address.orEmpty()} (${it.start_date.orEmpty()} ~ ${it.end_date.orEmpty()})"
                }
                dataBinding.tvBasicAreas.text = basic.ifBlank { "등록된 기본 지역이 없습니다." }
                dataBinding.tvAddedAreas.text = added.ifBlank { "등록된 추가 지역이 없습니다." }
            }
        }
    }
}
