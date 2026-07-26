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
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomDateRangeDialog
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
    private val removedAreaIds = mutableListOf<Int>()
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val saveFormatter: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE

    override fun init() {
        super.init()

        dataBinding.ivBack.setOnClickListener { findNavController().popBackStack() }
        dataBinding.tvAdd.setOnClickListener { addSecondaryArea() }
        dataBinding.tvDone.setOnClickListener { saveAreas() }

        bindCard(dataBinding.area1, index = 0)
        bindCard(dataBinding.area2, index = 1)
        areas.add(AddedAreaEditUiModel())
        render()
        reloadProfile()
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

        parentFragmentManager.setFragmentResultListener(
            "fromPlayerJoinSearch",
            viewLifecycleOwner
        ) { _, bundle ->
            val result = bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                ?: return@setFragmentResultListener
            val isStart = bundle.getBoolean("isStart", true)
            val isSecondary = bundle.getBoolean("isSecondary", false)
            val areaRadius = bundle.getInt("areaRadius", 3)
            val index = if (isSecondary) 1 else 0
            val area = areas.getOrNull(index) ?: return@setFragmentResultListener

            if (isStart) {
                area.depart = result
                area.departRadius = areaRadius
            } else {
                area.dest = result
                area.destRadius = areaRadius
            }
            area.addressChanged = true
            if (areas.size == 1 && area.isComplete()) {
                area.enable = true
            }
            render()
        }
    }

    private fun reloadProfile() {
        viewModel.getMyProfile {
            viewModel.getPlayerProfile { profile ->
                areas.clear()
                removedAreaIds.clear()
                profile.areas_added.orEmpty()
                    .take(2)
                    .mapTo(areas) { it.toEditUiModel() }
                if (areas.isEmpty()) {
                    areas.add(AddedAreaEditUiModel())
                }
                normalizeEnabledAreas()
                render()
            }
        }
    }

    private fun bindCard(binding: LayoutPlayerAreaAddedCardBinding, index: Int) {
        binding.tvDateSelect.setOnClickListener { openDateRangePicker(index) }
        binding.tvDateValue.setOnClickListener { openDateRangePicker(index) }
        binding.tvDepartSelect.setOnClickListener { openSearch(index, isStart = true) }
        binding.clDepart.setOnClickListener { openSearch(index, isStart = true) }
        binding.tvArriveSelect.setOnClickListener { openSearch(index, isStart = false) }
        binding.clArrive.setOnClickListener { openSearch(index, isStart = false) }
        binding.tvOn.setOnClickListener { toggleArea(index) }
        binding.ivDelete.setOnClickListener { deleteArea(index) }
    }

    private fun openDateRangePicker(index: Int) {
        val area = areas.getOrNull(index) ?: return
        BottomDateRangeDialog().apply {
            startDate = area.startDate
            endDate = area.endDate
            rangeSelectCallback = { start, end ->
                area.startDate = start
                area.endDate = end
                if (areas.size == 1 && area.isComplete()) {
                    area.enable = true
                }
                render()
            }
        }.show(parentFragmentManager, "BottomDateRangeDialog")
    }

    private fun openSearch(index: Int, isStart: Boolean) {
        if (index >= areas.size) return
        findNavController().navigate(
            R.id.action_global_playerJoinSearchFragment,
            Bundle().apply {
                putBoolean("isStart", isStart)
                putBoolean("isSecondary", index == 1)
            }
        )
    }

    private fun toggleArea(index: Int) {
        val area = areas.getOrNull(index) ?: return
        if (areas.size == 1) {
            area.enable = true
        } else {
            if (!area.enable) {
                area.enable = true
                areas.forEachIndexed { i, item ->
                    if (i != index) item.enable = false
                }
            }
        }
        render()
    }

    private fun addSecondaryArea() {
        if (areas.size >= 2 || !areas.first().isComplete()) return
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
            area.areaId?.let { removedAreaIds.add(it) }
            areas.removeAt(index)
            normalizeEnabledAreas()
            render()
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
                    depart = it.depart,
                    departRadius = it.departRadius,
                    dest = it.dest,
                    destRadius = it.destRadius
                )
            },
            removedAreaIds = removedAreaIds
        ) { success ->
            if (success) {
                requireContext().toast("추가 지역이 설정되었습니다.")
                findNavController().popBackStack()
            } else {
                requireContext().toast("추가 지역 설정에 실패했습니다.")
            }
        }
    }

    private fun render() {
        renderCard(dataBinding.area1, areas.getOrNull(0), 0)
        renderCard(dataBinding.area2, areas.getOrNull(1), 1)
        dataBinding.area2.root.isVisible = areas.size > 1
        dataBinding.tvAdd.isVisible = areas.size < 2
        dataBinding.tvAdd.isEnabled = areas.firstOrNull()?.isComplete() == true
        dataBinding.tvAdd.alpha = if (dataBinding.tvAdd.isEnabled) 1f else 0.4f
        dataBinding.tvDone.isEnabled = canSave()
    }

    private fun renderCard(
        binding: LayoutPlayerAreaAddedCardBinding,
        area: AddedAreaEditUiModel?,
        index: Int
    ) {
        if (area == null) {
            binding.tvTitle.text = "추가 동행 범위 ${index + 1}"
            binding.tvOn.isSelected = false
            binding.tvOn.text = "OFF"
            return
        }

        binding.tvTitle.text = "추가 동행 범위 ${index + 1}"
        binding.ivDelete.isVisible = index > 0
        binding.tvOn.isSelected = area.enable
        binding.tvOn.text = if (area.enable) "ON" else "OFF"

        // 픽업 가능일
        val hasDate = area.startDate != null && area.endDate != null
        binding.tvDateSelect.isVisible = !hasDate
        binding.tvDateValue.isVisible = hasDate
        if (hasDate) {
            binding.tvDateValue.text =
                "${area.startDate!!.format(displayFormatter)} - ${area.endDate!!.format(displayFormatter)}"
        }

        // 출발지
        binding.tvDepartSelect.isVisible = area.depart == null
        binding.clDepart.isVisible = area.depart != null
        binding.tvDepartValue.text = area.depart?.name.orEmpty()
        binding.tvDepartRange.text = "${area.departRadius ?: 3}KM"

        // 도착지
        binding.tvArriveSelect.isVisible = area.dest == null
        binding.clArrive.isVisible = area.dest != null
        binding.tvArriveValue.text = area.dest?.name.orEmpty()
        binding.tvArriveRange.text = "${area.destRadius ?: 3}KM"
    }

    private fun normalizeEnabledAreas() {
        if (areas.size == 1) {
            return
        }
        if (areas.none { it.enable }) {
            areas.firstOrNull()?.enable = true
        }
    }

    private fun canSave(): Boolean {
        if (areas.any { !it.isComplete() }) return false
        if (areas.none { it.enable }) return false
        return removedAreaIds.isNotEmpty() || areas.any {
            it.areaId == null || it.addressChanged || it.dateChanged ||
                it.originalEnable != it.enable
        }
    }

    private fun PlayerProfileDto.PlayerArea.toEditUiModel(): AddedAreaEditUiModel {
        val start = parseDate(start_date)
        val end = parseDate(end_date)
        return AddedAreaEditUiModel(
            areaId = player_area_id,
            originalEnable = use_yn == "Y",
            enable = use_yn == "Y",
            depart = KakaoSearchModel(
                name = depart_address.orEmpty(),
                lat = depart_latitude,
                lng = depart_longitude,
                subtitle = depart_address2,
                distance = null,
                roadAddress = depart_address2,
                source = "SAVED"
            ),
            departRadius = depart_range ?: 3,
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
        var depart: KakaoSearchModel? = null,
        var departRadius: Int? = null,
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
            depart != null &&
                dest != null &&
                startDate != null &&
                endDate != null &&
                departRadius != null &&
                destRadius != null &&
                depart?.lat != null &&
                depart?.lng != null &&
                dest?.lat != null &&
                dest?.lng != null
    }
}
