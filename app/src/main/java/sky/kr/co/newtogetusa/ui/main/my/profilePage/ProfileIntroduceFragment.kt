package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentProfileIntroduceBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.my.ProfileManagementViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ProfileIntroduceFragment : BaseFragment<FragmentProfileIntroduceBinding, ProfileManagementViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_profile_introduce
    override val viewModel: ProfileManagementViewModel by viewModels({requireParentFragment()})

    override fun init() {
        super.init()
        val isFromSearchResult = arguments?.getBoolean(ARG_FROM_SEARCH_RESULT, false) ?: false
        dataBinding.btnEditIntroduction.isVisible = !isFromSearchResult
        dataBinding.btnEditArea.isVisible = !isFromSearchResult
        dataBinding.btnEditAddArea.isVisible = !isFromSearchResult
        dataBinding.tvAddAreaDesc.isVisible = !isFromSearchResult

        // 부모에서 프로필을 비동기로 로드하므로, player_id가 준비되면 플레이어 프로필을 조회한다.
        // (init() 시점에 바로 호출하면 profileDto가 null이라 리스너가 연결되지 않는 문제 방지)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileDto.filterNotNull().collectLatest { profile ->
                    if ((profile.user.player_id ?: 0) > 0) {
                        viewModel.getPlayerProfile { playerProfile ->
                            bindPlayerProfile(playerProfile)
                        }
                    }
                }
            }
        }
    }

    private fun bindPlayerProfile(profile: PlayerProfileDto) = with(dataBinding) {
        // 자기 소개
        val introduction = profile.introduction.orEmpty()
        if (introduction.isEmpty()) {
            tvIntroduce.text = "자기 소개가 없습니다"
            tvIntroduce.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black_40))
        } else {
            tvIntroduce.text = introduction
            tvIntroduce.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black_80))
        }

        btnEditIntroduction.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileManagementFragment_to_writeIntroductionFragment,
                bundleOf(
                    "playerId" to (profile.player_id ?: profile.player.player_id),
                    "introduction" to profile.introduction.orEmpty()
                )
            )
        }

        // 동행 가능지역
        btnEditArea.setOnClickListener {
            findNavController().navigate(R.id.action_profileManagementFragment_to_playerAreaSettingFragment)
        }

        val hasBasicArea = !profile.areas_basic.isNullOrEmpty()
        tvAreaEmpty.isVisible = !hasBasicArea
        bindBasicArea(profile.areas_basic?.getOrNull(0), tvArea1)
        bindBasicArea(profile.areas_basic?.getOrNull(1), tvArea2)

        // 추가 지역 설정 (iOS SettingAreaOtherViewController 대응)
        btnEditAddArea.setOnClickListener {
            findNavController().navigate(R.id.action_profileManagementFragment_to_playerAreaAddedSettingFragment)
        }

        val hasAddedArea = !profile.areas_added.isNullOrEmpty()
        tvAddAreaEmpty.isVisible = !hasAddedArea
        btnEditAddArea.text = if (hasAddedArea) "설정" else "추가"

        bindAddedArea(profile.areas_added?.getOrNull(0), tvAddArea1, tvAddPickupDate1)
        bindAddedArea(profile.areas_added?.getOrNull(1), tvAddArea2, tvAddPickupDate2)
    }

    private fun bindBasicArea(
        area: PlayerProfileDto.PlayerArea?,
        areaView: TextView
    ) {
        val isVisible = area != null
        areaView.isVisible = isVisible
        if (area == null) return

        areaView.text = formatRoute(area.depart_address, area.dest_address)
    }

    private fun bindAddedArea(
        area: PlayerProfileDto.PlayerArea?,
        areaView: TextView,
        dateView: TextView
    ) {
        val isVisible = area != null
        areaView.isVisible = isVisible
        dateView.isVisible = isVisible
        if (area == null) return

        areaView.text = formatRoute(area.depart_address, area.dest_address)
        dateView.text = "픽업 가능일 ${formatDate(area.start_date)} - ${formatDate(area.end_date)}"
    }

    private fun formatRoute(departAddress: String?, destAddress: String?): String =
        "${departAddress.orEmpty()} > ${destAddress.orEmpty()}"

    private fun formatDate(rawDate: String?): String {
        val raw = rawDate.orEmpty().substringBefore(" ")
        val patterns = listOf("yyyyMMdd", "yyyy-MM-dd", "yyyy.MM.dd")

        patterns.forEach { pattern ->
            runCatching {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern))
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
        }

        return raw
    }

    companion object {
        const val ARG_FROM_SEARCH_RESULT = "ARG_FROM_SEARCH_RESULT"
    }
}
