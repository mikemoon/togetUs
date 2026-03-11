package sky.kr.co.newtogetusa.ui.main.history

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
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
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.route.RouteLinePattern
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHistoryDetailBinding
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomMoreDialog
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryRequestSharedViewModel
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import sky.kr.co.newtogetusa.utils.HorizontalItemSpacingDecoration
import sky.kr.co.newtogetusa.utils.MapUtil.drawRouteOnKakaoMap
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class HistoryDetailFragment : BaseFragment<FragmentHistoryDetailBinding, HistoryDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_history_detail
    override val viewModel: HistoryDetailViewModel by viewModels()

    private val deliverySharedViewModel: DeliveryRequestSharedViewModel
            by navGraphViewModels(R.id.nav_graph)

    private val args: HistoryDetailFragmentArgs by navArgs()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    //카카오
    private var isMapReady = false
    private var kakaoMap: KakaoMap? = null
    private var kakaoLabel: com.kakao.vectormap.label.Label? = null

    private var lastKnownLocation: Location? = null

    //구글
    private var googleMap: GoogleMap? = null

    private val FADE_START_Y = 120.dpToPx()   // 거의 상단일 때
    private val FADE_END_Y = 0              // 완전 상단
    var isExpanded = false

    @Inject
    lateinit var directionsRepo: DirectionsRepository

    //카카오 경로
    private var startLabel: com.kakao.vectormap.label.Label? = null
    private var destLabel: com.kakao.vectormap.label.Label? = null

    private var startStyles: LabelStyles? = null
    private var destStyles: LabelStyles? = null

    //사진
    private lateinit var photoAdapter: HistoryDetailPhotoAdapter

    override fun init() {
        super.init()

        Timber.d("delInfo ${args.delivery}")

        viewModel.getDeliveryDetailInfo(args.delivery.delivery_id)

        checkLocationPermission()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            setupGoogleMap()
        } else {
            setupKakaoMap()
        }
        val sheet = dataBinding.bottomSheet
        val behavior = BottomSheetBehavior.from(sheet)
        behavior.isFitToContents = false
        behavior.expandedOffset = 60.dpToPx()   // 탑바 높이
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val top = bottomSheet.top

                val progress = ((FADE_START_Y - top).toFloat() /
                        (FADE_START_Y - FADE_END_Y))
                    .coerceIn(0f, 1f)
                //Timber.d("onSlide $progress, ${bottomSheet.y}")
                if (progress >= 0.5 && behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    return
                }
                dataBinding.toolbar.apply {
                    alpha = progress
                    isVisible = true // 항상 켜두고 alpha로만 제어
                }

                dataBinding.clTop.apply {
                    alpha = 1f - progress
                    isVisible = true
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                //Timber.d("onStateChanged $newState")
                when (newState) {

                    BottomSheetBehavior.STATE_SETTLING -> {
                    }

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
                }
            }
        })

        photoAdapter = HistoryDetailPhotoAdapter { selectedItem ->
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.historyDetailFragment) {
                val photos = viewModel.deliveryDetail.value
                    ?.product
                    ?.pictures
                    ?.toTypedArray()
                    ?: emptyArray()
                val direction =
                    HistoryDetailFragmentDirections.actionHistoryDetailFragmentToPhotoFragment(
                        photos
                    )
                navController.navigate(direction)
            }
        }
        dataBinding.rvPhotos.apply {
            adapter = photoAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            itemAnimator = null
            if (itemDecorationCount == 0) addItemDecoration(
                HorizontalItemSpacingDecoration(
                    8.dpToPx(),
                    showFirst = false
                )
            )
        }
    }

    override fun initObserver() {
        super.initObserver()

        parentFragmentManager.setFragmentResultListener(
            "BottomMoreResult",
            viewLifecycleOwner
        ) { _, bundle ->
            Timber.d("BottomMoreResult ${bundle.getString("action")}")
            when (bundle.getString("action")) {
                "Cancel" -> {
                    cancelRequest()
                }

                "Modify" -> {}
                "Chatting" -> {}
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryDetail.filterNotNull().collectLatest { detail ->
                    updateBottomActionButtons(detail.status_cd)
                    if (isMapReady) {
                        drawRouteKakaoMapByDirections(
                            startLat = detail.depart.latitude,
                            startLng = detail.depart.longitude,
                            endLat = detail.dest.latitude,
                            endLng = detail.dest.longitude
                        )
                    }
                    photoAdapter.setItems(detail.product.pictures)
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is HistoryDetailViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                HistoryDetailViewModel.Event.MoreShow -> {
                    BottomMoreDialog().show(parentFragmentManager, "BottomMoreDialog")
                }

                HistoryDetailViewModel.Event.CancelReq -> {
                    cancelRequest()
                }
                HistoryDetailViewModel.Event.Modify ->{
                    val detail = viewModel.deliveryDetail.value ?: return@observe
                    populateDeliverySharedState()

                    // 9️⃣ DeliveryReqFragment로 이동
                    findNavController().navigate(
                        R.id.action_global_to_home_for_delivery,
                        bundleOf("openDeliveryReq" to true)
                    )
                }

                HistoryDetailViewModel.Event.ModifyFee ->{
                    if (findNavController().currentDestination?.id != R.id.historyDetailFragment) {
                        return@observe
                    }
                    populateDeliverySharedState()
                    findNavController().navigate(
                        R.id.action_global_to_home_for_delivery,
                        bundleOf("openDeliveryFee" to true)
                    )
                }
            }
        }
    }

    private fun populateDeliverySharedState() {
        val detail = viewModel.deliveryDetail.value ?: return
        deliverySharedViewModel.clearState()

        deliverySharedViewModel.updateDistance(
            distance = detail.expected.expected_distance.toString()
        )

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
            volume = detail.product.volume_cd
        )

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
            viewModel.cancelReq(
                viewModel.deliveryDetail.value?.delivery_id ?: return@onRightBtn
            ) { result ->
                if(result) {
                    requireContext().toast("동행요청 취소가 완료되었어요.")
                }
            }
        }.show(childFragmentManager, "")
    }

    private fun updateBottomActionButtons(statusCode: String) = with(dataBinding) {
        when (statusCode) {
            "REGISTER_ING" -> {
                llBottomButtonContainer.visibility = View.VISIBLE
                vBottomDivider.visibility = View.VISIBLE

                tvBottomSecondaryButton.apply {
                    visibility = View.VISIBLE
                    text = "삭제하기"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
                    setBackgroundResource(R.drawable.background_s_b5_r4)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 1f
                        marginEnd = 0
                    }
                    setOnClickListener {
                        this@HistoryDetailFragment.viewModel.onEventClick(HistoryDetailViewModel.Event.CancelReq)
                    }
                }

                tvBottomPrimaryButton.apply {
                    text = "수정하기"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setBackgroundResource(R.drawable.background_s_p100_r4)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 2f
                        marginStart = 12.dpToPx()
                    }
                    setOnClickListener {
                        this@HistoryDetailFragment.viewModel.onEventClick(HistoryDetailViewModel.Event.Modify)
                    }
                }
            }

            "MATCH_BEFORE" -> {
                llBottomButtonContainer.visibility = View.VISIBLE
                vBottomDivider.visibility = View.VISIBLE

                tvBottomSecondaryButton.apply {
                    visibility = View.VISIBLE
                    text = "취소하기"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
                    setBackgroundResource(R.drawable.background_s_b5_r4)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 1f
                        marginEnd = 0
                    }
                    setOnClickListener {
                        this@HistoryDetailFragment.viewModel.onEventClick(HistoryDetailViewModel.Event.CancelReq)
                    }
                }

                tvBottomPrimaryButton.apply {
                    text = "추가금액수정"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setBackgroundResource(R.drawable.background_s_p100_r4)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = 0
                        weight = 2f
                        marginStart = 12.dpToPx()
                    }
                    setOnClickListener {
                        this@HistoryDetailFragment.viewModel.onEventClick(HistoryDetailViewModel.Event.ModifyFee)
                    }
                }
            }

            "CANCEL" -> {
                llBottomButtonContainer.visibility = View.VISIBLE
                vBottomDivider.visibility = View.VISIBLE

                tvBottomSecondaryButton.visibility = View.GONE

                tvBottomPrimaryButton.apply {
                    text = "다시등록하기"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setBackgroundResource(R.drawable.background_s_p100_r4)
                    (layoutParams as LinearLayout.LayoutParams).apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        weight = 0f
                        marginStart = 0
                    }
                    setOnClickListener {
                        this@HistoryDetailFragment.viewModel.onEventClick(HistoryDetailViewModel.Event.Modify)
                    }
                }
            }

            else -> {
                llBottomButtonContainer.visibility = View.GONE
                vBottomDivider.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKakaoMap() {
        dataBinding.map.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    //if (isFollowMode) disableFollowMode()
                }
            }
            false // 지도 터치는 계속 처리되도록 false
        }

        dataBinding.map.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                isMapReady = false
            }

            override fun onMapError(p0: Exception?) {
            }

        }, object : KakaoMapReadyCallback() {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onMapReady(map: KakaoMap) {
                Timber.d("onMapReady")
                kakaoMap = map
                isMapReady = true

                startStyles = map.labelManager?.addLabelStyles(
                    LabelStyles.from(LabelStyle.from(R.drawable.pin_depart))
                )

                destStyles = map.labelManager?.addLabelStyles(
                    LabelStyles.from(LabelStyle.from(R.drawable.pin_arrive))
                )


                viewModel.deliveryDetail.value?.let { detail ->
                    drawRouteKakaoMapByDirections(
                        startLat = detail.depart.latitude,
                        startLng = detail.depart.longitude,
                        endLat = detail.dest.latitude,
                        endLng = detail.dest.longitude
                    )
                }
                //showMyLocation()
            }
        })
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupGoogleMap() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    lastKnownLocation = location
                    maybeInitMapWithLocation()
                }
            }

        //google map
        dataBinding.googleMap.getMapAsync { map ->
            googleMap = map
            googleMap?.uiSettings?.apply {
                isZoomControlsEnabled = false
                isMyLocationButtonEnabled = false
            }
            enableMyLocation()
            map.setOnMapClickListener { latLng ->
                //onMapTapped(latLng)
            }
            maybeInitMapWithLocation()
            viewModel.deliveryDetail.value?.let { detail ->
                drawRouteGoogleMap(
                    start = LatLng(detail.depart.latitude, detail.depart.longitude),
                    end = LatLng(detail.dest.latitude, detail.dest.longitude)
                )
            }
        }
    }


    //카카오 경로그리기
    private fun drawRouteKakaoMapByDirections(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val (points, summary) = directionsRepo.fetchRoute(
                startLat = startLat,
                startLng = startLng,
                endLat = endLat,
                endLng = endLng
            )

            if (points.isEmpty()) return@launch

            // 기존 핀 제거
            startLabel?.remove()
            destLabel?.remove()

            val startLL = com.kakao.vectormap.LatLng.from(startLat, startLng)
            val destLL = com.kakao.vectormap.LatLng.from(endLat, endLng)

            // 출발 / 도착 핀
            startLabel = layer.addLabel(
                LabelOptions.from(startLL).setStyles(startStyles)
            )
            destLabel = layer.addLabel(
                LabelOptions.from(destLL).setStyles(destStyles)
            )

            // ✅ 실제 경로 polyline
            drawRouteOnKakaoMap(map, points, summary)

            // 화면 맞추기
            val bb = com.kakao.vectormap.LatLngBounds.Builder()
            points.forEach { bb.include(it) }
            bb.include(startLL)
            bb.include(destLL)

            map.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(
                    bb.build(),
                    120
                )
            )
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
        //dataBinding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onResume()
        } else {
            dataBinding.map.resume()
        }
        //showStartLocation()
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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 이미 허용됨
            enableMyLocation()
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    //구글맵
    private fun maybeInitMapWithLocation() {
        if (googleMap != null && lastKnownLocation != null) {
            val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
            //googleMap?.addMarker(MarkerOptions().position(latLng).title("내 위치"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        if (lastKnownLocation == null) {//서울
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.5665, 126.9780), 12f))
        }
    }

    // 내 위치 활성화 (GoogleMap 객체가 초기화된 후 호출)
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        }
    }


    //구글 직선라인
    private fun drawRouteGoogleMap(
        start: LatLng,
        end: LatLng
    ) {
        val map = googleMap ?: return

        // 출발 마커
        map.addMarker(
            MarkerOptions()
                .position(start)
                .title("출발")
        )

        // 도착 마커
        map.addMarker(
            MarkerOptions()
                .position(end)
                .title("도착")
        )

        // 직선 Polyline
        map.addPolyline(
            PolylineOptions()
                .add(start, end)
                .width(8f)
                .color(ContextCompat.getColor(requireContext(), R.color.primary_100))
        )

        // 카메라 영역 맞추기
        val bounds = LatLngBounds.builder()
            .include(start)
            .include(end)
            .build()

        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, 100)
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한 허용됨
                enableMyLocation()
            } else {
                // 권한 거부됨
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}