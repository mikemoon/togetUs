package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
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
        viewModel.getPlayerProfile { profile ->
            bindPlayerProfile(profile)
        }
    }

    private fun bindPlayerProfile(profile: PlayerProfileDto) = with(dataBinding) {
        tvIntroduce.text = profile.introduction.orEmpty()
        btnEditIntroduction.setOnClickListener {
            findNavController().navigate(
                R.id.writeIntroductionFragment,
                bundleOf(
                    "playerId" to (profile.player_id ?: profile.player.player_id),
                    "introduction" to profile.introduction.orEmpty()
                )
            )
        }
        btnEditArea.setOnClickListener {
            findNavController().navigate(R.id.playerAreaSettingFragment)
        }

        bindBasicArea(profile.areas_basic?.getOrNull(0), tvArea1)
        bindBasicArea(profile.areas_basic?.getOrNull(1), tvArea2)

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
        val isFromSearchResult = arguments?.getBoolean(ARG_FROM_SEARCH_RESULT, false) ?: false
        val isVisible = area != null
        areaView.isVisible = isVisible
        dateView.isVisible = isVisible && !isFromSearchResult
        if (area == null) return

        areaView.text = formatRoute(area.depart_address, area.dest_address)
        dateView.text = "${formatDate(area.start_date)} ~ ${formatDate(area.end_date)}"
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
