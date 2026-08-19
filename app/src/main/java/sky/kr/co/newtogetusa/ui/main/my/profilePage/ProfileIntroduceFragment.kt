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

    private var isFromSearchResult = false

    override fun init() {
        super.init()
        isFromSearchResult = arguments?.getBoolean(ARG_FROM_SEARCH_RESULT, false) ?: false
        dataBinding.btnEditIntroduction.isVisible = !isFromSearchResult
        dataBinding.btnEditArea.isVisible = !isFromSearchResult
        dataBinding.btnEditAddArea.isVisible = !isFromSearchResult

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
        btnEditArea.text = if (hasBasicArea) "수정" else "추가"
        // 남의 프로필에서는 안내 문구 없이 첫 줄만 노출
        if (isFromSearchResult) {
            tvAreaEmpty.text = tvAreaEmpty.text.toString().substringBefore("\n")
            tvAddAreaEmpty.text = tvAddAreaEmpty.text.toString().substringBefore("\n")
        }
        bindBasicArea(profile.areas_basic?.getOrNull(0), tvArea1, 0)
        bindBasicArea(profile.areas_basic?.getOrNull(1), tvArea2, 1)

        // 추가 지역 설정 (iOS SettingAreaOtherViewController 대응)
        btnEditAddArea.setOnClickListener {
            findNavController().navigate(R.id.action_profileManagementFragment_to_playerAreaAddedSettingFragment)
        }

        val hasAddedArea = !profile.areas_added.isNullOrEmpty()
        tvAddAreaEmpty.isVisible = !hasAddedArea
        btnEditAddArea.text = if (hasAddedArea) "설정" else "추가"

        bindAddedArea(profile.areas_added?.getOrNull(0), tvAddArea1, tvAddPickupDate1, 0)
        bindAddedArea(profile.areas_added?.getOrNull(1), tvAddArea2, tvAddPickupDate2, 1)
    }

    private fun bindBasicArea(
        area: PlayerProfileDto.PlayerArea?,
        areaView: TextView,
        index: Int
    ) {
        val isVisible = area != null
        areaView.isVisible = isVisible
        if (area == null) return

        val route = listOf(toShortAddress(area.depart_address), toShortAddress(area.dest_address))
            .filter { it.isNotEmpty() }
            .joinToString("  →  ")
        areaView.text = buildCourseText(index + 1, route)
    }

    private fun bindAddedArea(
        area: PlayerProfileDto.PlayerArea?,
        areaView: TextView,
        dateView: TextView,
        index: Int
    ) {
        val isVisible = area != null
        areaView.isVisible = isVisible
        dateView.isVisible = isVisible
        if (area == null) return

        // 여행 동행은 픽업지(출발지) 없이 도착지만 표기
        areaView.text = buildCourseText(index + 1, toShortAddress(area.dest_address))

        val period = availablePeriodText(area)
        dateView.isVisible = period.isNotEmpty()
        dateView.text = period
    }

    // iOS ProfileCourseRowView 대응: "코스 1" (회색) + 경로 (검정) 스타일 분리
    private fun buildCourseText(index: Int, route: String): CharSequence {
        val full = "코스 $index  $route"
        val spannable = android.text.SpannableString(full)
        val gray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black_40)
        val black = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black_80)
        val indexEnd = "코스 $index".length
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(gray), 0, indexEnd,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            android.text.style.AbsoluteSizeSpan(14, true), 0, indexEnd,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(black), indexEnd, full.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    // "여행 일정 2026.08.01 - 2026.08.15" 형태 (날짜 없으면 빈 문자열 → 미노출)
    private fun availablePeriodText(area: PlayerProfileDto.PlayerArea): String {
        val start = formatDateOrNull(area.start_date)
        val end = formatDateOrNull(area.end_date)
        return when {
            start != null && end != null -> "여행 일정 $start - $end"
            start != null -> "여행 일정 $start -"
            end != null -> "여행 일정 - $end"
            else -> ""
        }
    }

    private fun formatDateOrNull(rawDate: String?): String? {
        val raw = rawDate.orEmpty().substringBefore(" ")
        if (raw.isBlank()) return null
        val patterns = listOf("yyyyMMdd", "yyyy-MM-dd", "yyyy.MM.dd")
        patterns.forEach { pattern ->
            runCatching {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern))
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
        }
        return null
    }

    // iOS areaDepth2 대응: 상세주소는 노출하지 않고 앞 2토큰(지역 2뎁스)까지만 표기
    private fun toShortAddress(address: String?): String =
        address.orEmpty().trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString(" ")

    companion object {
        const val ARG_FROM_SEARCH_RESULT = "ARG_FROM_SEARCH_RESULT"
    }
}
