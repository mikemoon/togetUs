package sky.kr.co.newtogetusa.ui.main.history.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHistoryDeliveryDetailBinding
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.ui.MainActivity
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.player.BottomDeliveryApplyDialog
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRequestSharedViewModel
import sky.kr.co.newtogetusa.ui.main.history.HistoryDetailPhotoAdapter
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import sky.kr.co.newtogetusa.utils.HorizontalItemSpacingDecoration
import sky.kr.co.newtogetusa.utils.MapUtil.drawRouteOnKakaoMap
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PlayerHistoryDetailFragment :
    BaseFragment<FragmentHistoryDeliveryDetailBinding, PlayerHistoryDetailViewModel>() {

    override val layoutId: Int = R.layout.fragment_history_delivery_detail
    override val viewModel: PlayerHistoryDetailViewModel by viewModels()

    private val deliverySharedViewModel: DeliveryRequestSharedViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private val args: PlayerHistoryDetailFragmentArgs by navArgs()
    private var kakaoMap: KakaoMap? = null
    private var googleMap: GoogleMap? = null
    private var isMapReady = false
    private var startLabel: com.kakao.vectormap.label.Label? = null
    private var destLabel: com.kakao.vectormap.label.Label? = null
    private var startStyles: LabelStyles? = null
    private var destStyles: LabelStyles? = null
    private lateinit var photoAdapter: HistoryDetailPhotoAdapter

    // iOS 대응: 툴�바 페이드는 시트 확장 직전 구간(60dp)에서만 진행하고,
    // 완전히 펼쳐지는 지점(expandedOffset)에서 정확히 alpha=1이 되도록 한다.
    private val expandedOffsetY by lazy { 60.dpToPx() }
    private val fadeStartY by lazy { expandedOffsetY + 60.dpToPx() }
    private val fadeEndY by lazy { expandedOffsetY }

    @Inject
    lateinit var directionsRepo: DirectionsRepository

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel

        viewModel.getDeliveryDetailInfo(args.deliveryId)

        // 동행 푸시 수신 시 같은 건을 보고 있으면 상세 재조회 (iOS refreshDeliveryInfo 대응)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRefreshBus.detailEvents.collect { id ->
                    if (id == args.deliveryId) {
                        viewModel.getDeliveryDetailInfo(id)
                    }
                }
            }
        }

        setupMap()
        setupBottomSheet()
        setupPhotos()

        dataBinding.ivBack.setOnClickListener {
            viewModel.onBackClick()
        }
        dataBinding.ivToolbarBack.setOnClickListener {
            viewModel.onBackClick()
        }

        dataBinding.tvBottomSecondaryButton.setOnClickListener {
            viewModel.onSecondaryClick()
        }

        dataBinding.tvBottomPrimaryButton.setOnClickListener {
            viewModel.onPrimaryClick()
        }
        dataBinding.ivHeart.setOnClickListener {
            viewModel.onMarkClick()
        }
        dataBinding.ivToolbarHeart.setOnClickListener {
            viewModel.onMarkClick()
        }
        dataBinding.ivMore.setOnClickListener {
            showMoreDialog()
        }
        dataBinding.ivToolbarMore.setOnClickListener {
            showMoreDialog()
        }

        // iOS 대응: 지도 클릭 시 맵 크게보기 화면으로 이동
        dataBinding.vMapClickOverlay.setOnClickListener {
            openMapDetail()
        }
    }

    private fun openMapDetail() {
        val detail = viewModel.deliveryDetail.value ?: return
        findNavController().navigate(
            PlayerHistoryDetailFragmentDirections.actionPlayerHistoryDetailFragmentToMapDetailFragment(
                startLat = detail.depart.latitude.toFloat(),
                startLng = detail.depart.longitude.toFloat(),
                endLat = detail.dest.latitude.toFloat(),
                endLng = detail.dest.longitude.toFloat(),
                startAddress = detail.depart.address,
                endAddress = detail.dest.address
            )
        )
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deliveryDetail.filterNotNull().collectLatest { detail ->
                        drawRoute(
                            startLat = detail.depart.latitude,
                            startLng = detail.depart.longitude,
                            endLat = detail.dest.latitude,
                            endLng = detail.dest.longitude
                        )
                        photoAdapter.setItems(detail.product.pictures)
                        dataBinding.llPhotos.isVisible = detail.product.pictures.isNotEmpty()
                        updateMarkIcon(detail.is_like)
                        updateMoreIconVisibility()
                    }
                }

                launch {
                    viewModel.buttonState.collectLatest { buttonState ->
                        if (buttonState == PlayerHistoryDetailViewModel.ButtonState.Hidden) {
                            dataBinding.llBottomButtonContainer.visibility = View.GONE
                            dataBinding.vBottomDivider.visibility = View.GONE
                            return@collectLatest
                        }

                        dataBinding.llBottomButtonContainer.visibility = View.VISIBLE
                        dataBinding.vBottomDivider.visibility = View.VISIBLE
                        dataBinding.tvBottomPrimaryButton.text = buttonState.primaryText

                        if (buttonState.showSecondary) {
                            dataBinding.tvBottomSecondaryButton.visibility = View.VISIBLE
                            dataBinding.tvBottomSecondaryButton.text = buttonState.secondaryText
                        } else {
                            dataBinding.tvBottomSecondaryButton.visibility = View.GONE
                        }

                        if (
                            buttonState == PlayerHistoryDetailViewModel.ButtonState.PlayerDone ||
                            buttonState == PlayerHistoryDetailViewModel.ButtonState.RequesterDone ||
                            buttonState == PlayerHistoryDetailViewModel.ButtonState.WaitUserConfirm
                        ) {
                            dataBinding.tvBottomPrimaryButton.setBackgroundResource(R.drawable.background_s_b5_r4)
                            dataBinding.tvBottomPrimaryButton.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.black_60)
                            )
                        } else {
                            dataBinding.tvBottomPrimaryButton.setBackgroundResource(R.drawable.background_s_p100_r4)
                            dataBinding.tvBottomPrimaryButton.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        }

                        // 유저 수령확인 대기 버튼은 비활성
                        dataBinding.tvBottomPrimaryButton.isEnabled =
                            buttonState != PlayerHistoryDetailViewModel.ButtonState.WaitUserConfirm
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PlayerHistoryDetailViewModel.Event.Back -> findNavController().popBackStack()
                is PlayerHistoryDetailViewModel.Event.ActionSuccess -> requireContext().toast(event.msg)
                is PlayerHistoryDetailViewModel.Event.ActionFail -> requireContext().toast("요청 처리에 실패했어요.")
                is PlayerHistoryDetailViewModel.Event.ShowApplyDialog -> {
                    val nickname = viewModel.deliveryDetail.value?.requester_rating?.nickname.orEmpty()
                    BottomDeliveryApplyDialog().apply {
                        requesterNickname = nickname
                        confirmCallback = { this@PlayerHistoryDetailFragment.viewModel.confirmApply() }
                        noticeCallback = {
                            dismissAllowingStateLoss()
                            findNavController().navigate(
                                PlayerHistoryDetailFragmentDirections.actionPlayerHistoryDetailFragmentToPlayerDeliveryNoticeFragment()
                            )
                        }
                    }.show(childFragmentManager, "BottomDeliveryApplyDialog")
                }
                is PlayerHistoryDetailViewModel.Event.Chat -> (requireActivity() as MainActivity).selectMainTab(R.id.chat)
                is PlayerHistoryDetailViewModel.Event.OpenChatRoom -> {
                    findNavController().navigate(
                        Uri.parse("togetus://chat-room/${event.roomId}")
                    )
                }
                is PlayerHistoryDetailViewModel.Event.DoneInfo -> requireContext().toast("이미 완료된 요청입니다.")
                is PlayerHistoryDetailViewModel.Event.CancelReq -> cancelRequest()
                is PlayerHistoryDetailViewModel.Event.ConfirmReport -> confirmReport()
                is PlayerHistoryDetailViewModel.Event.ConfirmCancelSupport -> confirmCancelSupport()
                is PlayerHistoryDetailViewModel.Event.OpenReportReason -> {
                    findNavController().navigate(
                        PlayerHistoryDetailFragmentDirections
                            .actionPlayerHistoryDetailFragmentToDeliveryReportReasonFragment(event.userId)
                    )
                }
                is PlayerHistoryDetailViewModel.Event.Modify -> {
                    populateDeliverySharedState()
                    findNavController().navigate(
                        R.id.action_global_to_home_for_delivery,
                        bundleOf(
                            "openDeliveryReq" to true,
                            "returnToHistory" to false,
                            "returnToDetail" to true,
                            "isEdit" to true,
                            "deliveryId" to args.deliveryId
                        )
                    )
                }
                is PlayerHistoryDetailViewModel.Event.ModifyFee -> {
                    requireActivity().findNavController(R.id.nav_host_container).navigate(
                        R.id.action_global_deliveryFeeFragment,
                        bundleOf("deliveryId" to args.deliveryId),
                        navOptions {
                            launchSingleTop = true
                        }
                    )
                }
                is PlayerHistoryDetailViewModel.Event.OpenReport -> {
                    findNavController().navigate(
                        PlayerHistoryDetailFragmentDirections.actionPlayerHistoryDetailFragmentToDeliveryReviewFragment(
                            args.deliveryId,
                            event.isPlayer
                        )
                    )
                }
                is PlayerHistoryDetailViewModel.Event.OpenProofPhoto -> {
                    findNavController().navigate(
                        R.id.action_global_takePhotoFragment,
                        bundleOf("deliveryId" to event.deliveryId, "proofType" to event.proofType)
                    )
                }
            }
        }
    }

    private fun showMoreDialog() {
        val actions = viewModel.getMoreActions()
        if (actions.isEmpty()) return

        val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dpToPx(), 0, 20.dpToPx(), 0)
        }

        actions.forEach { action ->
            container.addView(
                AppCompatTextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        56.dpToPx()
                    )
                    gravity = Gravity.CENTER_VERTICAL
                    text = action.label
                    setTextAppearance(R.style.Paragraph17R)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
                    setOnClickListener {
                        dialog.dismiss()
                        viewModel.onMoreActionClick(action)
                    }
                }
            )
        }

        dialog.setContentView(container)
        dialog.show()
    }

    private fun updateMoreIconVisibility() {
        val hasMoreAction = viewModel.getMoreActions().isNotEmpty()
        dataBinding.ivMore.isVisible = hasMoreAction
        dataBinding.ivToolbarMore.isVisible = hasMoreAction
    }

    private fun confirmReport() {
        MessageDialog.newInstance(
            msgTitle = "신고하기",
            msg = "이 사용자를 신고하시겠어요?",
            rightBtn = "확인",
            leftBtn = "취소"
        ).onRightBtn {
            viewModel.reportRequester()
        }.show(childFragmentManager, "ReportRequesterDialog")
    }

    private fun confirmCancelSupport() {
        MessageDialog.newInstance(
            msgTitle = "지원 취소",
            msg = "지원을 취소하시겠어요?",
            rightBtn = "지원 취소",
            leftBtn = "확인"
        ).onRightBtn {
            viewModel.confirmCancelSupport()
        }.show(childFragmentManager, "CancelSupportDialog")
    }

    private fun updateMarkIcon(isLiked: Boolean) {
        dataBinding.ivHeart.setImageResource(
            if (isLiked) R.drawable.ic_heart_fill else R.drawable.ic_heart
        )
        dataBinding.ivToolbarHeart.setImageResource(
            if (isLiked) R.drawable.heart_fill_primary else R.drawable.heart_line
        )
    }

    private fun populateDeliverySharedState() {
        val detail = viewModel.deliveryDetail.value ?: return
        deliverySharedViewModel.clearState()
        deliverySharedViewModel.updateDistance(detail.expected.expected_distance.toString())
        deliverySharedViewModel.updateStartLocation(
            address = detail.depart.address,
            detail = detail.depart.address2.orEmpty(),
            lat = detail.depart.latitude,
            lng = detail.depart.longitude
        )
        deliverySharedViewModel.updateDestinationLocation(
            address = detail.dest.address,
            detail = detail.dest.address2.orEmpty(),
            lat = detail.dest.latitude,
            lng = detail.dest.longitude
        )
        deliverySharedViewModel.updatePickupInfo(
            isImmediately = detail.pickup.is_immediately,
            date = detail.pickup.date,
            time = detail.pickup.time.orEmpty(),
            isFaceToFace = detail.pickup.is_face2face
        )
        deliverySharedViewModel.updateProductInfo(
            title = detail.product.name,
            description = detail.product.descript,
            type = detail.product.type_cd,
            weight = detail.product.weight_cd,
            volume = detail.product.volume_cd,
            typeLabel = viewModel.productTypeLabel(detail.product.type_cd),
            weightLabel = viewModel.productWeightLabel(detail.product.weight_cd),
            volumeLabel = viewModel.productVolumeLabel(detail.product.volume_cd)
        )
        deliverySharedViewModel.updateProductImages(detail.product.pictures)
        deliverySharedViewModel.updateUser(
            name = detail.depart_contact.name.orEmpty(),
            phone = detail.depart_contact.phone.orEmpty()
        )
        deliverySharedViewModel.setInternational(!detail.is_domestic)
    }

    private fun cancelRequest() {
        MessageDialog.newInstance(
            msg = "동행요청을 취소하시겠어요?",
            rightBtn = "예",
            leftBtn = "취소"
        ).onRightBtn {
            viewModel.cancelReq(args.deliveryId) { result ->
                requireContext().toast(
                    if (result) "동행요청 취소가 완료되었어요." else "동행요청 취소에 실패했어요."
                )
            }
        }.show(childFragmentManager, "")
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(dataBinding.bottomSheet)
        behavior.isFitToContents = false
        behavior.expandedOffset = expandedOffsetY
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val top = bottomSheet.top
                val progress = ((fadeStartY - top).toFloat() / (fadeStartY - fadeEndY))
                    .coerceIn(0f, 1f)

                // iOS 대응: 플로팅 버튼(clTop)은 항상 유지하고,
                // 불투명 흰색 툴�바(toolbar)가 위에서 페이드 인되며 덮는다.
                // alpha=0일 때는 INVISIBLE로 두어 잔상/터치 간섭을 방지한다.
                if (progress > 0f) {
                    dataBinding.toolbar.apply {
                        if (!isVisible) isVisible = true
                        alpha = progress
                    }
                } else {
                    dataBinding.toolbar.apply {
                        alpha = 0f
                        if (isVisible) isVisible = false
                    }
                }
                dataBinding.clTop.alpha = 1f
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        dataBinding.toolbar.apply {
                            alpha = 1f
                            isVisible = true
                        }
                        dataBinding.clTop.alpha = 1f
                    }
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        dataBinding.toolbar.apply {
                            alpha = 0f
                            isVisible = false
                        }
                        dataBinding.clTop.apply {
                            alpha = 1f
                            isVisible = true
                        }
                    }
                    else -> Unit
                }
            }
        })
    }

    private fun setupPhotos() {
        photoAdapter = HistoryDetailPhotoAdapter {}
        dataBinding.rvPhotos.apply {
            adapter = photoAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            itemAnimator = null
            if (itemDecorationCount == 0) {
                addItemDecoration(HorizontalItemSpacingDecoration(8.dpToPx(), showFirst = false))
            }
        }
    }

    private fun setupMap() {
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            setupGoogleMap()
        } else {
            setupKakaoMap()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKakaoMap() {
        dataBinding.map.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                isMapReady = false
            }

            override fun onMapError(error: Exception?) = Unit
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                isMapReady = true
                startStyles = map.labelManager?.addLabelStyles(
                    LabelStyles.from(LabelStyle.from(R.drawable.pin_depart))
                )
                destStyles = map.labelManager?.addLabelStyles(
                    LabelStyles.from(LabelStyle.from(R.drawable.pin_arrive))
                )
                viewModel.deliveryDetail.value?.let {
                    drawRoute(it.depart.latitude, it.depart.longitude, it.dest.latitude, it.dest.longitude)
                }
            }
        })
    }

    private fun setupGoogleMap() {
        dataBinding.googleMap.getMapAsync { map ->
            googleMap = map
            googleMap?.uiSettings?.apply {
                isZoomControlsEnabled = false
                isMyLocationButtonEnabled = false
            }
            enableMyLocation()
            viewModel.deliveryDetail.value?.let {
                drawRoute(it.depart.latitude, it.depart.longitude, it.dest.latitude, it.dest.longitude)
            }
        }
    }

    private fun drawRoute(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            drawRouteGoogleMap(LatLng(startLat, startLng), LatLng(endLat, endLng))
        } else if (isMapReady) {
            drawRouteKakaoMapByDirections(startLat, startLng, endLat, endLng)
        }
    }

    private fun drawRouteKakaoMapByDirections(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            startLabel?.remove()
            destLabel?.remove()

            val startLL = com.kakao.vectormap.LatLng.from(startLat, startLng)
            val destLL = com.kakao.vectormap.LatLng.from(endLat, endLng)
            startLabel = layer.addLabel(LabelOptions.from(startLL).setStyles(startStyles))
            destLabel = layer.addLabel(LabelOptions.from(destLL).setStyles(destStyles))

            val routeResult = runCatching {
                directionsRepo.fetchRoute(startLat, startLng, endLat, endLng)
            }.onFailure {
                Timber.e(it, "Kakao route request failed")
            }.getOrNull()

            val points = routeResult?.first.orEmpty()
            val summary = routeResult?.second
            if (points.isNotEmpty()) {
                drawRouteOnKakaoMap(map, points, summary)
                return@launch
            }

            moveKakaoCameraToPins(map, startLL, destLL)
        }
    }

    private fun moveKakaoCameraToPins(
        map: KakaoMap,
        start: com.kakao.vectormap.LatLng,
        dest: com.kakao.vectormap.LatLng
    ) {
        runCatching {
            val bounds = com.kakao.vectormap.LatLngBounds.Builder()
                .include(start)
                .include(dest)
                .build()
            map.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bounds, 120)
            )
        }.onFailure {
            map.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(start)
            )
        }
    }

    private fun drawRouteGoogleMap(start: LatLng, end: LatLng) {
        val map = googleMap ?: return
        map.clear()
        map.addMarker(MarkerOptions().position(start).title("출발"))
        map.addMarker(MarkerOptions().position(end).title("도착"))
        map.addPolyline(
            PolylineOptions()
                .add(start, end)
                .width(8f)
                .color(ContextCompat.getColor(requireContext(), R.color.primary_100))
        )
        runCatching {
            val bounds = LatLngBounds.builder().include(start).include(end).build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }.onFailure {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 12f))
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getDeliveryDetailInfo(args.deliveryId)
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onResume()
        } else {
            dataBinding.map.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onPause()
        } else {
            dataBinding.map.pause()
        }
        isMapReady = false
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onStop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onLowMemory()
        }
    }
}
