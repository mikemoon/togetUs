package sky.kr.co.newtogetusa.ui.main.history.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.player.BottomDeliveryApplyDialog
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

    private val args: PlayerHistoryDetailFragmentArgs by navArgs()
    private var kakaoMap: KakaoMap? = null
    private var googleMap: GoogleMap? = null
    private var isMapReady = false
    private var startLabel: com.kakao.vectormap.label.Label? = null
    private var destLabel: com.kakao.vectormap.label.Label? = null
    private var startStyles: LabelStyles? = null
    private var destStyles: LabelStyles? = null
    private lateinit var photoAdapter: HistoryDetailPhotoAdapter

    private val fadeStartY by lazy { 120.dpToPx() }
    private val fadeEndY = 0

    @Inject
    lateinit var directionsRepo: DirectionsRepository

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel

        viewModel.getDeliveryDetailInfo(args.deliveryId)
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

                        if (buttonState == PlayerHistoryDetailViewModel.ButtonState.Done) {
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
                is PlayerHistoryDetailViewModel.Event.Chat -> requireContext().toast("채팅 기능을 준비중입니다.")
                is PlayerHistoryDetailViewModel.Event.DoneInfo -> requireContext().toast("이미 완료된 요청입니다.")
                is PlayerHistoryDetailViewModel.Event.OpenReport -> {
                    findNavController().navigate(
                        PlayerHistoryDetailFragmentDirections.actionPlayerHistoryDetailFragmentToReportFragment()
                    )
                }
            }
        }
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(dataBinding.bottomSheet)
        behavior.isFitToContents = false
        behavior.expandedOffset = 60.dpToPx()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val top = bottomSheet.top
                val progress = ((fadeStartY - top).toFloat() / (fadeStartY - fadeEndY))
                    .coerceIn(0f, 1f)

                dataBinding.toolbar.apply {
                    alpha = progress
                    isVisible = true
                }
                dataBinding.clTop.apply {
                    alpha = 1f - progress
                    isVisible = true
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        dataBinding.toolbar.apply {
                            alpha = 1f
                            isVisible = true
                        }
                        dataBinding.clTop.apply {
                            alpha = 0f
                            isVisible = false
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
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
