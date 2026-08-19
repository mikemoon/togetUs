package sky.kr.co.newtogetusa.ui.main.my

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.data.remote.dto.player.PlayerProfileDto
import sky.kr.co.newtogetusa.databinding.FragmentPlayerAreaAddedSettingBinding
import sky.kr.co.newtogetusa.databinding.LayoutPlayerAreaAddedCardBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading
import sky.kr.co.newtogetusa.utils.toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class PlayerAreaAddedSettingFragment :
    BaseFragment<FragmentPlayerAreaAddedSettingBinding, ProfileManagementViewModel>() {

    override val layoutId: Int = R.layout.fragment_player_area_added_setting
    override val viewModel: ProfileManagementViewModel by viewModels()

    private val areas = mutableListOf<AddedAreaEditUiModel>()
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val saveFormatter: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE

    override fun init() {
        super.init()

        dataBinding.ivBack.setOnClickListener { findNavController().popBackStack() }
        dataBinding.tvAdd.setOnClickListener { addSecondaryArea() }
        dataBinding.tvDone.setOnClickListener { saveAreas() }

        bindCard(dataBinding.area1, index = 0)
        bindCard(dataBinding.area2, index = 1)
        // 주소/날짜 선택 화면에서 돌아올 때 뷰만 재생성되므로(Fragment 인스턴스 유지),
        // 최초 진입 시에만 프로필을 다시 불러온다. 재조회하면 선택한 값이 서버 값으로 덮어씌워진다.
        if (areas.isEmpty()) {
            areas.add(AddedAreaEditUiModel())
            render()
            reloadProfile()
        } else {
            render()
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadingState.collect { isLoading ->
                        if (isLoading) showLoading() else hideLoading()
                    }
                }
            }
        }

        // 여행 지역(도착지) 주소 검색 결과
        requireActivity().supportFragmentManager.setFragmentResultListener(
            "fromPlayerJoinSearch",
            viewLifecycleOwner
        ) { _, bundle ->
            val result = bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                ?: return@setFragmentResultListener
            val isSecondary = bundle.getBoolean("isSecondary", false)
            val areaRadius = bundle.getInt("areaRadius", 3)
            val index = if (isSecondary) 1 else 0
            val area = areas.getOrNull(index) ?: return@setFragmentResultListener

            val wasIncomplete = !area.isComplete()
            area.dest = result
            area.destRadius = areaRadius
            area.addressChanged = true
            // 일정 + 여행 지역이 모두 채워지는 시점에 자동 ON
            if (wasIncomplete && area.isComplete()) {
                area.enable = true
            }
            render()
        }

        // 여행 일정 선택 화면(SelectDateRangeFragment) 결과
        requireActivity().supportFragmentManager.setFragmentResultListener(
            "fromSelectDateRange",
            viewLifecycleOwner
        ) { _, bundle ->
            val index = bundle.getInt("areaIndex", 0)
            val area = areas.getOrNull(index) ?: return@setFragmentResultListener
            val wasIncomplete = !area.isComplete()
            area.startDate = parseDate(bundle.getString("startDate"))
            area.endDate = parseDate(bundle.getString("endDate"))
            // 일정 + 여행 지역이 모두 채워지는 시점에 자동 ON
            if (wasIncomplete && area.isComplete()) {
                area.enable = true
            }
            render()
        }
    }

    private fun reloadProfile() {
        viewModel.getMyProfile {
            viewModel.getPlayerProfile { profile ->
                areas.clear()
                profile.areas_added.orEmpty()
                    .take(2)
                    .mapTo(areas) { it.toEditUiModel() }
                if (areas.isEmpty()) {
                    areas.add(AddedAreaEditUiModel())
                }
                render()
            }
        }
    }

    private fun bindCard(binding: LayoutPlayerAreaAddedCardBinding, index: Int) {
        binding.tvDateSelect.setOnClickListener { openDateRangePicker(index) }
        binding.tvDateValue.setOnClickListener { openDateRangePicker(index) }
        binding.tvArriveSelect.setOnClickListener { openSearch(index) }
        binding.clArrive.setOnClickListener { openSearch(index) }
        binding.tvOn.setOnClickListener { toggleArea(index) }
        binding.ivDelete.setOnClickListener { deleteArea(index) }
    }

    private fun openDateRangePicker(index: Int) {
        if (index >= areas.size) return
        val area = areas[index]

        findNavController().navigate(
            R.id.action_global_selectDateRangeFragment,
            Bundle().apply {
                putInt("areaIndex", index)
                area.startDate?.let { putString("startDate", it.format(saveFormatter)) }
                area.endDate?.let { putString("endDate", it.format(saveFormatter)) }
            }
        )
    }

    private fun openSearch(index: Int) {
        if (index >= areas.size) return
        val area = areas[index]

        findNavController().navigate(
            R.id.action_global_playerJoinSearchFragment,
            Bundle().apply {
                putBoolean("isStart", false)
                putBoolean("isSecondary", index == 1)
                // iOS와 동일: 기존 주소가 있으면 바로 반경설정 모드로 진입
                val existingAddress = area.dest
                if (existingAddress != null && existingAddress.lat != null && existingAddress.lat > 0
                    && existingAddress.lng != null && existingAddress.lng > 0) {
                    putParcelable("existingAddress", existingAddress)
                    putInt("existingRadius", area.destRadius ?: 3)
                }
            }
        )
    }

    private fun toggleArea(index: Int) {
        val area = areas.getOrNull(index) ?: return
        // 일정 + 여행 지역이 모두 선택된 코스만 토글 가능. 전부 OFF도 저장 가능(임시 제외 의미).
        if (!area.isComplete()) return
        area.enable = !area.enable
        render()
    }

    private fun addSecondaryArea() {
        if (areas.size >= 2 || areas.firstOrNull()?.isComplete() != true) return
        areas.add(AddedAreaEditUiModel())
        render()
    }

    private fun deleteArea(index: Int) {
        val area = areas.getOrNull(index) ?: return
        if (index == 0) return

        MessageDialog.newInstance(
            msg = "정말 삭제하시겠어요?",
            leftBtn = "아니오",
            rightBtn = "예"
        ).onRightBtn {
            // iOS와 동일: 서버 등록 항목은 즉시 삭제 API 호출, 미등록 항목은 로컬에서만 제거
            val areaId = area.areaId
            if (areaId == null || areaId <= 0) {
                areas.removeAt(index)
                render()
                requireContext().toast("삭제가 완료되었습니다.")
            } else {
                viewModel.deletePlayerArea(areaId) { success ->
                    if (success) {
                        areas.removeAt(index)
                        render()
                        requireContext().toast("삭제가 완료되었습니다.")
                    } else {
                        requireContext().toast("삭제에 실패했습니다.")
                    }
                }
            }
        }.show(childFragmentManager, "DeleteAddedAreaDialog")
    }

    private fun saveAreas() {
        if (!canSave()) return
        viewModel.savePlayerAddedAreas(
            areas = areas.map {
                ProfileManagementViewModel.PlayerAddedAreaEdit(
                    areaId = it.areaId,
                    enable = it.enable,
                    start = it.startDate!!.format(saveFormatter),
                    end = it.endDate!!.format(saveFormatter),
                    dest = it.dest,
                    destRadius = it.destRadius
                )
            },
            // iOS와 동일하게 삭제는 즉시 처리되므로 저장 시 삭제할 항목 없음
            removedAreaIds = emptyList()
        ) { success ->
            if (success) {
                requireContext().toast("여행 동행이 설정되었습니다.")
                findNavController().popBackStack()
            } else {
                requireContext().toast("여행 동행 설정에 실패했습니다.")
            }
        }
    }

    private fun render() {
        renderCard(dataBinding.area1, areas.getOrNull(0), 0)
        renderCard(dataBinding.area2, areas.getOrNull(1), 1)
        dataBinding.area2.root.isVisible = areas.size > 1
        // 코스 1의 일정/지역 설정이 끝난 경우에만 노출. 코스가 2개면 숨김 (최대 2개)
        dataBinding.tvAdd.isVisible =
            areas.size == 1 && areas.firstOrNull()?.isComplete() == true
        dataBinding.tvDone.isEnabled = canSave()
    }

    private fun renderCard(
        binding: LayoutPlayerAreaAddedCardBinding,
        area: AddedAreaEditUiModel?,
        index: Int
    ) {
        if (area == null) {
            binding.tvTitle.text = "여행 동행 코스 ${index + 1}"
            binding.tvOn.isSelected = false
            binding.tvOn.text = "OFF"
            return
        }

        binding.tvTitle.text = "여행 동행 코스 ${index + 1}"
        binding.ivDelete.isVisible = index > 0
        // 일정/지역이 모두 선택되기 전에는 ON/OFF 자체를 누를 수 없다
        binding.tvOn.isEnabled = area.isComplete()
        binding.tvOn.isSelected = area.enable
        binding.tvOn.text = if (area.enable) "ON" else "OFF"

        // 여행 일정
        val hasDate = area.startDate != null && area.endDate != null
        binding.tvDateSelect.isVisible = !hasDate
        binding.tvDateValue.isVisible = hasDate
        if (hasDate) {
            binding.tvDateValue.text =
                "${area.startDate!!.format(displayFormatter)} - ${area.endDate!!.format(displayFormatter)}"
        }

        // 여행 지역 (도착지)
        binding.tvArriveSelect.isVisible = area.dest == null
        binding.clArrive.isVisible = area.dest != null
        binding.tvArriveValue.text = area.dest?.name.orEmpty()
        binding.tvArriveRange.text = "${area.destRadius ?: 3}KM"
    }

    // iOS: 모든 코스가 일정+여행 지역 완성이면 저장 가능 (전부 OFF도 허용)
    private fun canSave(): Boolean {
        if (areas.any { !it.isComplete() }) return false
        return areas.any {
            it.areaId == null || it.addressChanged || it.dateChanged ||
                it.originalEnable != it.enable
        }
    }

    private fun PlayerProfileDto.PlayerArea.toEditUiModel(): AddedAreaEditUiModel {
        val start = parseDate(start_date)
        val end = parseDate(end_date)
        // 여행 동행은 도착지(여행지)만 사용한다
        return AddedAreaEditUiModel(
            areaId = player_area_id,
            originalEnable = use_yn == "Y",
            enable = use_yn == "Y",
            dest = KakaoSearchModel(
                name = dest_address.orEmpty(),
                lat = dest_latitude,
                lng = dest_longitude,
                subtitle = dest_address2,
                distance = null,
                roadAddress = dest_address2,
                source = "SAVED"
            ),
            destRadius = dest_range ?: 3,
            startDate = start,
            endDate = end,
            originalStartDate = start,
            originalEndDate = end
        )
    }

    private fun parseDate(raw: String?): LocalDate? {
        val value = raw.orEmpty().substringBefore(" ")
        if (value.isBlank()) return null
        listOf("yyyyMMdd", "yyyy-MM-dd", "yyyy.MM.dd").forEach { pattern ->
            runCatching {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern))
            }
        }
        return null
    }

    private data class AddedAreaEditUiModel(
        val areaId: Int? = null,
        val originalEnable: Boolean = false,
        var enable: Boolean = false,
        var dest: KakaoSearchModel? = null,
        var destRadius: Int? = null,
        var startDate: LocalDate? = null,
        var endDate: LocalDate? = null,
        val originalStartDate: LocalDate? = null,
        val originalEndDate: LocalDate? = null,
        var addressChanged: Boolean = false
    ) {
        val dateChanged: Boolean
            get() = startDate != originalStartDate || endDate != originalEndDate

        fun isComplete(): Boolean =
            dest != null &&
                startDate != null &&
                endDate != null &&
                destRadius != null &&
                dest?.lat != null &&
                dest?.lng != null
    }
}
