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
import sky.kr.co.newtogetusa.databinding.FragmentPlayerAreaSettingBinding
import sky.kr.co.newtogetusa.databinding.LayoutPlayerAreaSettingCardBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.showLoading
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class PlayerAreaSettingFragment :
    BaseFragment<FragmentPlayerAreaSettingBinding, ProfileManagementViewModel>() {

    override val layoutId: Int = R.layout.fragment_player_area_setting
    override val viewModel: ProfileManagementViewModel by viewModels()

    private val areas = mutableListOf<AreaEditUiModel>()
    private val removedAreaIds = mutableListOf<Int>()
    private var originalGpsAlarmOn = false

    override fun init() {
        super.init()

        dataBinding.ivBack.setOnClickListener { findNavController().popBackStack() }
        dataBinding.tvAdd.setOnClickListener { addSecondaryArea() }
        dataBinding.tvDone.setOnClickListener { saveAreas() }

        bindCard(dataBinding.area1, index = 0)
        bindCard(dataBinding.area2, index = 1)
        // 주소 검색 화면에서 돌아올 때 뷰만 재생성되므로(Fragment 인스턴스 유지),
        // 최초 진입 시에만 프로필을 다시 불러온다. 재조회하면 선택한 주소가 서버 값으로 덮어씌워진다.
        if (areas.isEmpty()) {
            areas.add(AreaEditUiModel())
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

        requireActivity().supportFragmentManager.setFragmentResultListener(
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
                originalGpsAlarmOn = profile.gps_area
                dataBinding.swLocationAlarm.setOnCheckedChangeListener(null)
                dataBinding.swLocationAlarm.isChecked = profile.gps_area
                dataBinding.swLocationAlarm.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setPlayerGpsEnable(isChecked) { success ->
                        if (!success) {
                            requireContext().toast("현위치 동행 알림 설정에 실패했습니다.")
                            dataBinding.swLocationAlarm.setOnCheckedChangeListener(null)
                            dataBinding.swLocationAlarm.isChecked = !isChecked
                            dataBinding.swLocationAlarm.setOnCheckedChangeListener { _, checked ->
                                viewModel.setPlayerGpsEnable(checked)
                            }
                        } else {
                            originalGpsAlarmOn = isChecked
                        }
                    }
                }

                areas.clear()
                removedAreaIds.clear()
                profile.areas_basic.orEmpty()
                    .take(2)
                    .mapTo(areas) { it.toEditUiModel() }
                if (areas.isEmpty()) {
                    areas.add(AreaEditUiModel())
                }
                render()
            }
        }
    }

    private fun bindCard(binding: LayoutPlayerAreaSettingCardBinding, index: Int) {
        binding.tvDepartSelect.setOnClickListener { openSearch(index, isStart = true) }
        binding.clDepart.setOnClickListener { openSearch(index, isStart = true) }
        binding.tvArriveSelect.setOnClickListener { openSearch(index, isStart = false) }
        binding.clArrive.setOnClickListener { openSearch(index, isStart = false) }
        binding.tvOn.setOnClickListener { toggleArea(index) }
        binding.ivDelete.setOnClickListener { deleteArea(index) }
    }

    private fun openSearch(index: Int, isStart: Boolean) {
        if (index >= areas.size) return
        val area = areas[index]
        val existingAddress = if (isStart) area.depart else area.dest
        val existingRadius = if (isStart) area.departRadius else area.destRadius

        findNavController().navigate(
            R.id.action_global_playerJoinSearchFragment,
            Bundle().apply {
                putBoolean("isStart", isStart)
                putBoolean("isSecondary", index == 1)
                // iOS와 동일: 기존 주소가 있으면 바로 반경설정 모드로 진입
                if (existingAddress != null && existingAddress.lat != null && existingAddress.lat > 0
                    && existingAddress.lng != null && existingAddress.lng > 0) {
                    putParcelable("existingAddress", existingAddress)
                    putInt("existingRadius", existingRadius ?: 3)
                }
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
        areas.add(AreaEditUiModel())
        render()
    }

    private fun deleteArea(index: Int) {
        val area = areas.getOrNull(index) ?: return
        if (index == 0) return

        fun removeLocalArea() {
            area.areaId?.let { removedAreaIds.add(it) }
            areas.removeAt(index)
            normalizeEnabledAreas()
            render()
        }

        MessageDialog.newInstance(
            msg = "정말 삭제하시겠어요?",
            leftBtn = "아니오",
            rightBtn = "예"
        ).onRightBtn {
            removeLocalArea()
        }.show(childFragmentManager, "DeleteAreaDialog")
    }

    private fun saveAreas() {
        if (!canSave()) return
        viewModel.savePlayerBasicAreas(
            areas = areas.map {
                ProfileManagementViewModel.PlayerBasicAreaEdit(
                    areaId = it.areaId,
                    addressChanged = it.addressChanged,
                    enableChanged = it.originalEnable != it.enable,
                    enable = it.enable,
                    depart = it.depart,
                    departRadius = it.departRadius,
                    dest = it.dest,
                    destRadius = it.destRadius
                )
            },
            removedAreaIds = removedAreaIds
        ) { success ->
            if (success) {
                requireContext().toast("동행 가능지역이 설정되었습니다.")
                findNavController().popBackStack()
            } else {
                requireContext().toast("동행 가능지역 설정에 실패했습니다.")
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
        binding: LayoutPlayerAreaSettingCardBinding,
        area: AreaEditUiModel?,
        index: Int
    ) {
        if (area == null) {
            binding.tvTitle.text = "매일 동행 ${if (index == 0) "①" else "②"} 코스"
            binding.tvOn.isSelected = false
            binding.tvOn.text = "OFF"
            return
        }

        binding.tvTitle.text = "매일 동행 ${if (index == 0) "①" else "②"} 코스"
        binding.ivDelete.isVisible = index > 0
        binding.tvOn.isSelected = area.enable
        binding.tvOn.text = if (area.enable) "ON" else "OFF"

        binding.tvDepartSelect.isVisible = area.depart == null
        binding.clDepart.isVisible = area.depart != null
        binding.tvDepartValue.text = area.depart?.name.orEmpty()
        binding.tvDepartRange.text = "${area.departRadius ?: 3}KM"

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
            it.areaId == null || it.addressChanged || it.originalEnable != it.enable
        }
    }

    private fun PlayerProfileDto.PlayerArea.toEditUiModel(): AreaEditUiModel =
        AreaEditUiModel(
            areaId = player_area_id,
            originalEnable = use_yn == "Y",
            enable = false,
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
            destRadius = dest_range ?: 3
        )

    private data class AreaEditUiModel(
        val areaId: Int? = null,
        val originalEnable: Boolean = false,
        var enable: Boolean = false,
        var depart: KakaoSearchModel? = null,
        var departRadius: Int? = null,
        var dest: KakaoSearchModel? = null,
        var destRadius: Int? = null,
        var addressChanged: Boolean = false
    ) {
        fun isComplete(): Boolean =
            depart != null &&
                dest != null &&
                departRadius != null &&
                destRadius != null &&
                depart?.lat != null &&
                depart?.lng != null &&
                dest?.lat != null &&
                dest?.lng != null
    }
}
